import re
import io
import yaml
import argparse
import itertools
import collections
from pathlib import Path
yaml.add_representer(collections.defaultdict, yaml.representer.Representer.represent_dict)
nested_dict = lambda: collections.defaultdict(nested_dict)

def default_opennmt_config(model_dir, train_features_file, train_labels_file, eval_features_file, eval_labels_file, node_label_vocabulary_file, node_type_vocabulary_file, node_transformation_vocabulary_file):
    opennmt_config = nested_dict()

    # models_dir
    opennmt_config['model_dir'] = model_dir

    # data config
    opennmt_config['data']['train_features_file'] = train_features_file
    opennmt_config['data']['train_labels_file'] = train_labels_file
    opennmt_config['data']['eval_features_file'] = eval_features_file
    opennmt_config['data']['eval_labels_file'] = eval_labels_file
    opennmt_config['data']['node_label_vocabulary'] = node_label_vocabulary_file
    opennmt_config['data']['node_type_vocabulary'] = node_type_vocabulary_file
    opennmt_config['data']['node_transformation_vocabulary'] = node_transformation_vocabulary_file

    # params config
    opennmt_config['params']['optimizer'] = 'AdamOptimizer'
    opennmt_config['params']['optimizer_params']['beta1'] = 0.9
    opennmt_config['params']['optimizer_params']['beta2'] = 0.999
    opennmt_config['params']['learning_rate'] = 0.001

    opennmt_config['params']['clip_gradients'] = None

    opennmt_config['params']['regularization'] = None
    opennmt_config['params']['weight_decay'] = None

    opennmt_config['params']['average_loss_in_time'] = True

    opennmt_config['params']['decay_type'] = 'exponential_decay'
    opennmt_config['params']['decay_params']['decay_rate'] = 0.7
    opennmt_config['params']['decay_params']['decay_steps'] = 10000
    opennmt_config['params']['decay_params']['staircase'] = True
    opennmt_config['params']['decay_step_duration'] = 1
    opennmt_config['params']['start_decay_steps'] = 50000

    opennmt_config['params']['label_smoothing'] = 0.1

    opennmt_config['params']['beam_width'] = 3
    opennmt_config['params']['num_hypotheses'] = 0
    opennmt_config['params']['length_penalty'] = 0

    opennmt_config['params']['replace_unknown_target'] = False

    # train config
    opennmt_config['train']['batch_size'] = 32

    opennmt_config['train']['save_checkpoints_steps'] = 10000
    opennmt_config['train']['keep_checkpoint_max'] = 10

    opennmt_config['train']['train_steps'] = 300000
    opennmt_config['train']['max_step'] = 300000
    opennmt_config['train']['maximum_features_length'] = 400
    opennmt_config['train']['maximum_labels_length'] = 400

    # eval config
    opennmt_config['eval']['batch_size'] = 8

    opennmt_config['eval']['save_eval_predictions'] = True

    opennmt_config['eval']['eval_delay'] = 3600

    # infer config
    opennmt_config['infer']['batch_size'] = 1

    opennmt_config['infer']['n_best'] = 3
    opennmt_config['infer']['with_scores'] = False

    # score config
    opennmt_config['score']['batch_size'] = 8

    return opennmt_config

def default_dual_source_transformer_config(embedding_size=128, num_layers=6, num_units=128, num_heads=8, ffn_inner_dim=512, dropout=0.1, attention_dropout=0.1, relu_dropout=0.1):
    transformer_config = '''\
"""Defines a dual source Transformer architecture with serial attention layers
and parameter sharing between the encoders.

See for example https://arxiv.org/pdf/1809.00188.pdf.
"""
import opennmt as onmt

from opennmt.utils import misc


class DualSourceTransformer(onmt.models.Transformer):

  def __init__(self):
    super(DualSourceTransformer, self).__init__(
      source_inputter=onmt.inputters.ParallelInputter([
          onmt.inputters.WordEmbedder(
              vocabulary_file_key="node_label_vocabulary",
              embedding_size={embedding_size}),
          onmt.inputters.WordEmbedder(
              vocabulary_file_key="node_type_vocabulary",
              embedding_size={embedding_size})],
          reducer=onmt.layers.ConcatReducer()),
      target_inputter=onmt.inputters.WordEmbedder(
          vocabulary_file_key="node_transformation_vocabulary",
          embedding_size={embedding_size}),
      num_layers={num_layers},
      num_units={num_units},
      num_heads={num_heads},
      ffn_inner_dim={ffn_inner_dim},
      dropout={dropout},
      attention_dropout={attention_dropout},
      relu_dropout={relu_dropout})

  def auto_config(self, num_devices=1):
    config = super(DualSourceTransformer, self).auto_config(num_devices=num_devices)
    max_length = config["train"]["maximum_features_length"]
    return misc.merge_dict(config, {{
        "train": {{
            "maximum_features_length": [max_length, max_length]
        }}
    }})


model = DualSourceTransformer
    '''.format(
        embedding_size=embedding_size, num_layers=num_layers, num_units=num_units,
        num_heads=num_heads, ffn_inner_dim=ffn_inner_dim, dropout=dropout,
        attention_dropout=attention_dropout, relu_dropout=relu_dropout
    )
    return transformer_config

def default_hpc2n_job_script(opennmt_config_path, transformer_config_path, gpu_type='k80', number_of_gpus='1', time='12:00:00'):
    hpc2n_job_script = '''\
#!/bin/bash

# Project to run under
#SBATCH -A SNIC2019-3-453
# Name of the job (makes easier to find in the status lists)
#SBATCH -J repair
# Exclusive use when using more than 2 GPUs
#SBATCH --exclusive
# Use GPU
#SBATCH --gres=gpu:{gpu_type}:{number_of_gpus}
# the job can use up to 30 minutes to run
#SBATCH --time={time}

# run the program
onmt-main --config {opennmt_config_path} --model {transformer_config_path} train_and_eval --num_gpus {number_of_gpus}
    '''.format(
        gpu_type=gpu_type, number_of_gpus=number_of_gpus, time=time,
        opennmt_config_path=opennmt_config_path, transformer_config_path=transformer_config_path
    )
    return hpc2n_job_script


def update_learning_rate(config, transformer_config_str, learning_rate):
    config['params']['learning_rate'] = learning_rate
    return config, transformer_config_str

def update_train_batch_size(config, transformer_config_str, batch_size):
    config['train']['batch_size'] = batch_size
    return config, transformer_config_str

def update_embedding_size_and_num_units(config, transformer_config_str, size):
    embedding_config_str = 'embedding_size='+str(size)
    num_units_config_str = 'num_units='+str(size)
    transformer_config_str = re.sub(r'embedding_size=\d+(\.\d+)?', embedding_config_str, transformer_config_str)
    transformer_config_str = re.sub(r'num_units=\d+(\.\d+)?', num_units_config_str, transformer_config_str)
    return config, transformer_config_str

def update_num_layers(config, transformer_config_str, num_layers):
    num_layers_config_str = 'num_layers='+str(num_layers)
    transformer_config_str = re.sub(r'num_layers=\d+(\.\d+)?', num_layers_config_str, transformer_config_str)
    return config, transformer_config_str

def update_num_heads(config, transformer_config_str, num_heads):
    num_heads_config_str = 'num_heads='+str(num_heads)
    transformer_config_str = re.sub(r'num_heads=\d+(\.\d+)?', num_heads_config_str, transformer_config_str)
    return config, transformer_config_str

def update_share_encoders(config, transformer_config_str, share_encoders):
    share_encoders_config_str = 'share_encoders='+str(share_encoders)
    transformer_config_str = re.sub(r'share_encoders=(True|False)', share_encoders_config_str, transformer_config_str)
    return config, transformer_config_str

def update_ffn_inner_dim(config, transformer_config_str, ffn_inner_dim):
    ffn_inner_dim_config_str = 'ffn_inner_dim='+str(ffn_inner_dim)
    transformer_config_str = re.sub(r'ffn_inner_dim=\d+(\.\d+)?', ffn_inner_dim_config_str, transformer_config_str)
    return config, transformer_config_str

def main():
    parser = argparse.ArgumentParser(description='Automatic creating data.yml for OpenNMT-tf and copy all data.')
    parser.add_argument('-train_node_labels_file', action="store", dest='train_node_labels_file', help="Path to train_node_labels_file")
    parser.add_argument('-train_node_types_file', action="store", dest='train_node_types_file', help="Path to train_node_types_file")
    parser.add_argument('-train_node_transformations_file', action="store", dest='train_node_transformations_file', help="Path to train_node_transformations_file")
    parser.add_argument('-eval_node_labels_file', action="store", dest='eval_node_labels_file', help="Path to eval_node_labels_file")
    parser.add_argument('-eval_node_types_file', action="store", dest='eval_node_types_file', help="Path to eval_node_types_file")
    parser.add_argument('-eval_node_transformations_file', action="store", dest='eval_node_transformations_file', help="Path to eval_node_transformations_file")
    parser.add_argument('-node_label_vocabulary', action="store", dest='node_label_vocabulary', help="Path to node_label_vocabulary")
    parser.add_argument('-node_type_vocabulary', action="store", dest='node_type_vocabulary', help="Path to node_type_vocabulary")
    parser.add_argument('-node_transformation_vocabulary', action="store", dest='node_transformation_vocabulary', help="Path to node_transformation_vocabulary")
    parser.add_argument('-sweep_root_path', action="store", dest='sweep_root_path', help="Path to the root directory of all configs sweeps")
    args = parser.parse_args()

    train_node_labels_file = Path(args.train_node_labels_file).resolve()
    train_node_types_file = Path(args.train_node_types_file).resolve()
    train_node_transformations_file = Path(args.train_node_transformations_file).resolve()
    eval_node_labels_file = Path(args.eval_node_labels_file).resolve()
    eval_node_types_file = Path(args.eval_node_types_file).resolve()
    eval_node_transformations_file = Path(args.eval_node_transformations_file).resolve()
    node_label_vocabulary = Path(args.node_label_vocabulary).resolve()
    node_type_vocabulary = Path(args.node_type_vocabulary).resolve()
    node_transformation_vocabulary = Path(args.node_transformation_vocabulary).resolve()
    sweep_root_path = Path(args.sweep_root_path).resolve()

    for file in [train_node_labels_file, train_node_types_file, train_node_transformations_file, eval_node_labels_file, eval_node_types_file, eval_node_transformations_file, node_label_vocabulary, node_type_vocabulary, node_transformation_vocabulary, sweep_root_path]:
        assert(file.exists())

    learning_rate_sweep = list((update_learning_rate, learning_rate) for learning_rate in [0.001])
    embedding_size_and_num_units_sweep = list((update_embedding_size_and_num_units, size) for size in [128, 256])
    num_layers_sweep = list((update_num_layers, num_layers) for num_layers in [6])

    parameter_sweep = [learning_rate_sweep, num_layers_sweep, embedding_size_and_num_units_sweep]
    for index, updates in enumerate(itertools.product(*parameter_sweep)):
        sweep_path = sweep_root_path / (str(index) + '_parameter_sweep')
        sweep_path.mkdir(parents=True, exist_ok=True)

        model_dir = sweep_path / 'models'
        opennmt_config = default_opennmt_config(str(model_dir).replace('\\','\\\\'), [str(path).replace('\\','\\\\') for path in [train_node_labels_file, train_node_types_file]], str(train_node_transformations_file).replace('\\','\\\\'), [str(file).replace('\\','\\\\') for file in [eval_node_labels_file, eval_node_types_file]], str(eval_node_transformations_file).replace('\\','\\\\'), str(node_label_vocabulary).replace('\\','\\\\'), str(node_type_vocabulary).replace('\\','\\\\'), str(node_transformation_vocabulary).replace('\\','\\\\'))
        transformer_config = default_dual_source_transformer_config()
        for update_func, parameter in updates:
            opennmt_config, transformer_config = update_func(opennmt_config, transformer_config, parameter)

        opennmt_config_path = sweep_path / 'data.yml'
        with io.open(opennmt_config_path, 'w', encoding='utf8') as f:
            yaml.dump(opennmt_config, f, default_flow_style=False, allow_unicode=True, sort_keys=False)
        transformer_config_path = sweep_path / 'multi_source_transformer.py'
        with io.open(transformer_config_path, 'w', encoding='utf8') as f:
            f.write(transformer_config)
        hpc2n_job_script = default_hpc2n_job_script(str(opennmt_config_path).replace('\\','\\\\'), str(transformer_config_path).replace('\\','\\\\'))
        hpc2n_job_script_path = sweep_path / 'job.sh'
        with io.open(hpc2n_job_script_path, 'w', encoding='utf8') as f:
            f.write(hpc2n_job_script)


if __name__=="__main__":
    main()
