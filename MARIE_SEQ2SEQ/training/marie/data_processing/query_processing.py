from marie.utils import advance_ptr_thru_space, advance_ptr_to_kw, advance_ptr_to_space
from marie.data_processing.abstract_query_rep import AbstractQueryRep


QUERY_ENCODINGS = {
    "{": " op_br ",
    "}": " cl_br ",
    "?": " var_",
    "<": " lt ",
    ">": " gt ",
}
QUERY_DECODINGS = {v.strip(): k for k, v in QUERY_ENCODINGS.items()}


def replace_multi(text: str, mapper: dict):
    for k, v in mapper.items():
        text = text.replace(k, v)
    return text


def encode_special_chars(query: str):
    return replace_multi(query, QUERY_ENCODINGS)


def decode_special_chars(query: str):
    return replace_multi(query, QUERY_DECODINGS)


def remove_prefixes(query: str):
    idx = advance_ptr_to_kw(query, "PREFIX")
    if idx == len(query):
        return query

    while query.startswith("PREFIX", idx):
        # PREFIX prefix: <iri>
        idx += len("PREFIX")
        idx = advance_ptr_to_kw(query, ">", idx)
        idx += len(">")
        idx = advance_ptr_thru_space(query, idx)

    return query[idx:]


def preprocess_query(query: str):
    query = remove_prefixes(query)
    query = encode_special_chars(query)
    return query


def postprocess_query(query: str):
    query = decode_special_chars(query)
    try:
        query = AbstractQueryRep.from_string(query).compact2verbose().to_query_string()
    except:
        query = None
    return query


def normalize_query(query: str):
    for c in [".", ",", "{", "}", "(", ")", "<", ">", "&&", "||"]:
        query = query.replace(c, f" {c} ")

    return " ".join(query.split())
