package io.z77z.dao;

import io.z77z.entity.Funny;

public interface FunnyMapper {
    int deleteByPrimaryKey(Integer funnyId);

    int insert(Funny record);

    int insertSelective(Funny record);

    Funny selectByPrimaryKey(Integer funnyId);

    int updateByPrimaryKeySelective(Funny record);

    int updateByPrimaryKeyWithBLOBs(Funny record);

    int updateByPrimaryKey(Funny record);

	Funny selectByGroupId(String groupId);
}