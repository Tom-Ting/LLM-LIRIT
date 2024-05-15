from sentence_transformers import SentenceTransformer, util

model = SentenceTransformer("AlgorithmServer/models/sbert-base-chinese-nli")
print("完成模型加载")


def sentence_semantic_match(source_sentence: str, target_sentences: [str]):
    s_embedding = model.encode(source_sentence)
    t_embeddings = model.encode(target_sentences)
    res = util.semantic_search(s_embedding, t_embeddings, top_k=5)
    hit = res[0][0]
    print(source_sentence)
    print(target_sentences)
    return hit['corpus_id'], hit['score']


def sentence_semantic_multiply_match(source_sentence: [str], target_sentences: [str], threshold):
    s_embedding = model.encode(source_sentence)
    t_embeddings = model.encode(target_sentences)
    semantic_res = util.semantic_search(s_embedding, t_embeddings, top_k=5)
    res = [[-1, 0]] * len(semantic_res)
    for i in range(0, len(semantic_res)):
        if semantic_res[i][0]['score'] >= threshold:
            res[i] = [semantic_res[i][0]['corpus_id'], semantic_res[i][0]['score']]
    res.sort(key=lambda x: x[1],reverse=True)
    res_sort = [n[0] for n in res]
    return res_sort


if __name__ == '__main__':
    s = "总是允许"
    t = ["到", "允许", "同意"]
    ind, score = sentence_semantic_match(s, t)
    print(ind)
    print(score)
