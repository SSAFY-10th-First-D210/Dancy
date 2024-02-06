import { privateApi, publicApi } from '../util/http-commons'
import axios from 'axios';

const baseURL = 'http://i10d210.p.ssafy.io:8080'
const url = 'user'

export const userDetails = async () => {
  try {
    const res = await privateApi.get(`/${url}/details`);

    const userInfo = { 
      email: res.data.email,
      nickname: res.data.nickname,
      birthDate: res.data.birthDate,
      introduceText: res.data.introduceText,
      profileImageUrl: res.data.profileImageUrl,
    }

    return { userInfo }
  } catch (error) {
    console.error(error);
    throw error;
  }
};