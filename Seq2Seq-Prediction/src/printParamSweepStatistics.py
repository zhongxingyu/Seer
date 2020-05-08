import io
import pathlib
from pathlib import Path

def computeAcc(target_validation_file_path, prediction_file_path):
    # Opens prediction file (src_file) and ground-truth file (tgt_file)
    with io.open(prediction_file_path, mode="r", encoding="utf-8") as file:
        src_lines = file.readlines()
    with open(target_validation_file_path, mode="r", encoding="utf-8") as file:
        tgt_orig = file.readlines()

    # Remove leading and trailing spaces
    src_lines = [line.strip() for line in src_lines]
    tgt_lines = [line.strip() for line in tgt_orig]

    # if we don't have multiple predictions, it might because we reached
    # time limit in the middle of evalutaion.
    if len(src_lines) % len(tgt_lines) != 0:
        return 0

    beam_size = int(len(src_lines)/len(tgt_lines))

    # Count for correct prediction
    correct_count = 0
    top_n = 3

    for i in range(len(tgt_lines)):
        tgt_line = tgt_lines[i]

        # Remove all whitespaces
        tgt_line = ''.join(tgt_line.split())
        for j in range(beam_size):
            if j >= top_n:
                break
            src_line = src_lines[i*beam_size+j]

            # Remove all whitespaces
            src_line = ''.join(src_line.split())
            if(src_line == tgt_line):
                correct_count += 1

    return (correct_count/len(tgt_lines))*100

def main():
    experiment_path = pathlib.Path(__file__).absolute().parent.parent / 'experiment'
    for tree_setting_path in experiment_path.iterdir():
        target_validation_file_path = next(tree_setting_path.rglob('tgt-val-*.txt'))
        for parameter_sweep_path in tree_setting_path.rglob('*_parameter_sweep'):
            predictions_path = parameter_sweep_path / 'models' / 'eval'
            if predictions_path.exists():
                best_val_acc = 0
                max_step = -1
                best_step = -1
                for prediction in predictions_path.iterdir():
                    if prediction.name.startswith('predictions.txt'):
                        cur_step = int(prediction.name.split('.')[-1])
                        if cur_step > max_step:
                            max_step = cur_step
                        cur_val_acc = computeAcc(target_validation_file_path, prediction)
                        if cur_val_acc > best_val_acc:
                            best_val_acc = cur_val_acc
                            best_step = cur_step
                print('{} {}/{} {}%'.format(str(parameter_sweep_path), str(best_step), str(max_step), str(best_val_acc)))

if __name__=="__main__":
    main()
