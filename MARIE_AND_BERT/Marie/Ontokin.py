from Marie.QAEngine import QAEngine


class OntoKinQAEngine(QAEngine):

    def __init__(self, dataset_dir="CrossGraph/ontokin", dataset_name="ontokin", nel=None):
        super().__init__(dataset_dir, dataset_name, dim=80, nel=nel)


if __name__ == "__main__":
    my_engine = OntoKinQAEngine(dataset_dir="CrossGraph/ontokin", dataset_name="ontokin")
    rst = my_engine.run("what is co2's geometry")
    print(rst)
