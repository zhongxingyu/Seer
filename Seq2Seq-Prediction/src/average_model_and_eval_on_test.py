import io
import pathlib
import re
import os
from pathlib import Path

test_data_types = ['Method_RW_Method', 'Method_RW_Var', 'Multiple_Transform', 'VAR_RW_Method', 'VAR_RW_VAR', 'binOperatorModif', 'constChange', 'expLogicExpand', 'expLogicReduce', 'unwrapIfElse', 'unwrapMethod', 'wrapsIfElse_NULL', 'wrapsIfElse_Others', 'wrapsIf_NULL', 'wrapsIf_Others', 'wrapsMethod', 'wrapsTryCatch']

def main():
    experiment_path = pathlib.Path(__file__).absolute().parent.parent / 'experiment'
    for experiment_type_path in experiment_path.iterdir():
        for parameter_sweep_path in experiment_type_path.rglob('*_parameter_sweep'):
            models_path = parameter_sweep_path / 'models'
            if not models_path.exists():
                continue

            average_cmd = 'onmt-average-checkpoints'
            average_cmd += ' --model_dir {}'.format(str(models_path.resolve()))
            average_cmd += ' --output_dir {}'.format(str((models_path/'avg_5_model').resolve()))
            average_cmd += ' --max_count 5'
            print('Running cmd: ' + average_cmd)
            os.system(average_cmd)
            average_mode_path = (models_path / 'avg_5_model') / 'model.ckpt-300000'

            config_path = parameter_sweep_path / 'data.yml'
            for test_data_type in test_data_types:
                eval_test_cmd = 'onmt-main infer'
                eval_test_cmd += ' --config {}'.format(str(config_path.resolve()))
                test_node_labels_path = Path(str(experiment_type_path).replace('experiment', 'test_data')) / '{}_data_for_node_label.txt'.format(test_data_type)
                test_node_types_path = Path(str(experiment_type_path).replace('experiment', 'test_data')) / '{}_data_for_node_type.txt'.format(test_data_type)
                eval_test_cmd += ' --features_file {} {}'.format(str(test_node_labels_path.resolve()), str(test_node_types_path.resolve()))
                prediction_label_transform_path = parameter_sweep_path / '{}_data_for_node_transform.txt.prediction'.format(test_data_type)
                eval_test_cmd += ' --predictions_file {}'.format(str(prediction_label_transform_path.resolve()))
                eval_test_cmd += ' --checkpoint_path {}'.format(str(average_mode_path.resolve()))
                print('Running cmd: ' + eval_test_cmd)
                os.system(eval_test_cmd)


if __name__=="__main__":
    main()
