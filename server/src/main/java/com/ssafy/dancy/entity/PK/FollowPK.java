package com.ssafy.dancy.entity.PK;

import com.ssafy.dancy.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FollowPK implements Serializable {

    private User fromUser;

    private User toUser;
}
