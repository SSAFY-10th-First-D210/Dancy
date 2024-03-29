import { privateApi, publicApi } from '../util/http-commons'

const url = 'stage'

export const allArticles = async () => {
  try {
    const res = await privateApi.get(`/${url}`,   {params:{'limit': 200}} );
    const allArticles = res.data;
    return allArticles
  } catch (error) {
    console.error(error);
    throw error;
  }
};

export const postArticle = async (formData) => {
  try {
    const res = await privateApi.post(`/${url}`, formData);
    const articleInfo = res.data
		console.log(articleInfo)
    return articleInfo
  } catch (error) {
    console.error(error);
    throw error;
  }
};

export const getArticle = async (articleId) => {
  try {
    const res = await privateApi.get(`/${url}/${articleId}`);
    const articleInfo = res.data

    return articleInfo
  } catch (error) {
    console.error(error);
    throw error;
  }
}

export const deleteArticle = async (articleId) => {
  try {
    const res = await privateApi.delete(`/${url}/${articleId}`, {'articleId': articleId});

  } catch (error) {
    console.error(error)
    throw error;
  }
}

export const UpdateArticle = async ({articleId, formData}) => {
  try {
    const res = await privateApi.put(`/${url}/${articleId}`, formData);
    
    const updateInfo = res.data
		return updateInfo
  } catch (error) {
    console.error(error)
    throw error;
  }
}

export const saveArticle = async (articleId) => {
  try {
    const res = await privateApi.post(`/${url}/save/${articleId}`);
		const saveInfo = res.data
		// console.log(res.data)
		return saveInfo
  } catch (error) {
    console.error(error);
    throw error;
  }
};