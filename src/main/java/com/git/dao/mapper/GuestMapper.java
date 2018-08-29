package com.git.dao.mapper;

import com.git.dao.pojo.Guest;
import com.git.dao.pojo.GuestExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

public interface GuestMapper {
    int countByExample(GuestExample example);

    int deleteByExample(GuestExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(Guest record);

    int insertSelective(Guest record);

    List<Guest> selectByExampleWithRowbounds(GuestExample example, RowBounds rowBounds);

    List<Guest> selectByExample(GuestExample example);

    Guest selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") Guest record, @Param("example") GuestExample example);

    int updateByExample(@Param("record") Guest record, @Param("example") GuestExample example);

    int updateByPrimaryKeySelective(Guest record);

    int updateByPrimaryKey(Guest record);
}