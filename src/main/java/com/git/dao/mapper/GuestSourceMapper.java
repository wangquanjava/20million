package com.git.dao.mapper;

import com.git.dao.pojo.GuestSource;
import com.git.dao.pojo.GuestSourceExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

public interface GuestSourceMapper {
    int countByExample(GuestSourceExample example);

    int deleteByExample(GuestSourceExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(GuestSource record);

    int insertSelective(GuestSource record);

    List<GuestSource> selectByExampleWithBLOBsWithRowbounds(GuestSourceExample example, RowBounds rowBounds);

    List<GuestSource> selectByExampleWithBLOBs(GuestSourceExample example);

    List<GuestSource> selectByExampleWithRowbounds(GuestSourceExample example, RowBounds rowBounds);

    List<GuestSource> selectByExample(GuestSourceExample example);

    GuestSource selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") GuestSource record, @Param("example") GuestSourceExample example);

    int updateByExampleWithBLOBs(@Param("record") GuestSource record, @Param("example") GuestSourceExample example);

    int updateByExample(@Param("record") GuestSource record, @Param("example") GuestSourceExample example);

    int updateByPrimaryKeySelective(GuestSource record);

    int updateByPrimaryKeyWithBLOBs(GuestSource record);
}