import io
import pathlib
import re
import os
from pathlib import Path

test_data_types = ['Method_RW_Method', 'Method_RW_Var', 'Multiple_Transform', 'VAR_RW_Method', 'VAR_RW_VAR', 'binOperatorModif', 'constChange', 'expLogicExpand', 'expLogicReduce', 'unwrapIfElse', 'unwrapMethod', 'wrapsIfElse_NULL', 'wrapsIfElse_Others', 'wrapsIf_NULL', 'wrapsIf_Others', 'wrapsMethod', 'wrapsTryCatch']

def computeAcc(predictions_test_data_path, target_test_data_path):
    with io.open(predictions_test_data_path, mode="r", encoding="utf-8") as file:
        src_lines = file.readlines()
    with open(target_test_data_path, mode="r", encoding="utf-8") as file:
        tgt_orig = file.readlines()
    src_lines = [line.strip() for line in src_lines]
    tgt_lines = [line.strip() for line in tgt_orig]
    # Check if we have multiple predictions
    assert(len(src_lines) % len(tgt_lines) == 0)

    beam_size = int(len(src_lines)/len(tgt_lines))
    assert(beam_size == 3)

    # Count for correct prediction
    top_3_correct_count = 0

    for i in range(len(tgt_lines)):
        tgt_line = tgt_lines[i]

        # Remove all whitespaces
        tgt_line = ''.join(tgt_line.split())
        for j in range(beam_size):
            src_line = src_lines[i*beam_size+j]

            # Remove all whitespaces
            src_line = ''.join(src_line.split())
            if src_line == tgt_line:
                top_3_correct_count += 1
                break
    return top_3_correct_count/len(tgt_lines)*100


def main():
    experiment_path = pathlib.Path(__file__).absolute().parent.parent / 'experiment'
    for experiment_type_path in experiment_path.iterdir():
        for parameter_sweep_path in experiment_type_path.rglob('*_parameter_sweep'):
            for test_data_type in test_data_types:
                target_test_data_path = Path(str(experiment_type_path).replace('experiment', 'test_data')) / (test_data_type+'_data_for_node_transform.txt')
                predictions_test_data_path = parameter_sweep_path / (test_data_type+'_data_for_node_transform.txt.prediction')
                top3_acc = computeAcc(predictions_test_data_path, target_test_data_path)
                acc_str = '{} with {}. Top-3: {}%.'.format(experiment_type_path.name, test_data_type, str(top3_acc))
                print(acc_str)


if __name__=="__main__":
    main()
