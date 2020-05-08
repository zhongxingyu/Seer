import io
import sys
from random import shuffle
import math

def main(argv):
    argv = [int(i) for i in argv]
    assert(len(argv) == 2)
    assert(argv[0]+argv[1] == 100)

    with open("../training_data/training_data_for_node_label.txt", "r") as node_lable_file:
        node_labels = node_lable_file.readlines()

    with open("../training_data/training_data_for_node_type.txt", "r") as node_type_file:
        node_types = node_type_file.readlines()

    with open("../training_data/training_data_for_node_transform.txt", "r") as node_transform_file:
        node_transformations = node_transform_file.readlines()

    assert(len(node_labels) == len(node_types))
    assert(len(node_types) == len(node_transformations))

    zip_list_tmp = []
    for label, type, transform in zip(node_labels, node_types, node_transformations):
        if len(label.split(' ')) <= 200:
            zip_list_tmp.append((label, type, transform))
    shuffle(zip_list_tmp)

    node_labels, node_types, node_transformations = zip(*zip_list_tmp)

    total_count = len(zip_list_tmp)

    train_index = math.floor((argv[0]/100) * total_count)

    with io.open("src-train-node-labels-200-tokens.txt", "w", encoding="utf-8") as src_train_label_file:
        for i in range(train_index):
            src_train_label_file.write(node_labels[i])

    with io.open("src-train-node-types-200-tokens.txt", "w", encoding="utf-8") as src_train_type_file:
        for i in range(train_index):
            src_train_type_file.write(node_types[i])

    with io.open("tgt-train-node-transformations-200-tokens.txt", "w", encoding="utf-8") as tgt_train_file:
        for i in range(train_index):
            tgt_train_file.write(node_transformations[i])

    with io.open("src-val-node-labels-200-tokens.txt", "w", encoding="utf-8") as src_val_label_file:
        for i in range(train_index, total_count):
            src_val_label_file.write(node_labels[i])

    with io.open("src-val-node-types-200-tokens.txt", "w", encoding="utf-8") as src_val_type_file:
        for i in range(train_index, total_count):
            src_val_type_file.write(node_types[i])

    with io.open("tgt-val-node-transformations-200-tokens.txt", "w", encoding="utf-8") as tgt_val_file:
        for i in range(train_index, total_count):
            tgt_val_file.write(node_transformations[i])


if __name__=="__main__":
    main(sys.argv[1:])
