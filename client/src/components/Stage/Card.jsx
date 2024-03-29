import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import * as C from "./Card.Style";
import { allArticles } from "../../api/stage";

// 사용할 색상 배열
const colors = ["#fffbe5", "#d8fcf6", "#dfe5fe"];

export default function Card({ maxDisplay }) {
  const [articles, setArticles] = useState([]);

  useEffect(() => {
    allArticles()
      .then((res) => {
        setArticles(res);
        console.log(res);
      })
      .catch((err) => {
        console.error(err);
      });
  }, []);

  // 카드 개수 제한을 위해서 추가
  const displayedArticles = articles.slice(0, maxDisplay || articles.length);

  const cards = displayedArticles.map((item, index) => {
    const color = colors[Math.floor(index / 3) % colors.length];
    return (
      <Link to={`/detail/${item.articleId}`} key={index}>
        <C.CardContainer key={index}>
          <C.CardUpperContainer src={item.articleThumbnail} />
          <C.CardLowerContainer color={color}>
            <C.CardDetailContainer>
              <C.CardProfileImage src={item.authorProfileImage} />
              <C.CardDetailArea>
                <C.CardTitleDiv>
                  <C.CardTitle>{item.articleTitle}</C.CardTitle>
                </C.CardTitleDiv>
                <C.CardUserName>{item.authorName}</C.CardUserName>
                <C.CardViewAndDate>
                  조회 수 {item.articleView}회 | &nbsp;
                  {`${item.createdDate[0]}. ${item.createdDate[1]}. ${item.createdDate[2]}.`}
                </C.CardViewAndDate>
              </C.CardDetailArea>
            </C.CardDetailContainer>
          </C.CardLowerContainer>
        </C.CardContainer>
      </Link>
    );
  });

  return <>{cards}</>;
}
