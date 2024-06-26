package com.seckillflow.service.impl;

import com.seckillflow.domain.entity.SysResourceEntity;
import com.seckillflow.domain.entity.SysRoleResourceEntity;
import com.seckillflow.domain.entity.SysUserEntity;
import com.seckillflow.domain.PageResult;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhouzifei.common.constant.Constants;
import com.seckillflow.domain.TreeSelect;
import com.seckillflow.domain.model.SysRoleAuth;
import com.seckillflow.mapper.SysResourceMapper;
import com.seckillflow.mapper.SysRoleResourceMapper;
import com.seckillflow.service.SysResourceService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SysResourceServiceImpl extends ServiceImpl<SysResourceMapper, SysResourceEntity> implements SysResourceService {

    @Resource
    private SysResourceMapper resourceMapper;
    @Resource
    private SysRoleResourceMapper roleResourceMapper;

    @Override
    public PageResult<SysResourceEntity> page(SysResourceEntity sysResourceEntity) {
        return resourceMapper.selectPage(sysResourceEntity);
    }

    /**
     * 新增接口资源信息
     *
     * @param resource
     * @return
     */
    @Override
    public int insertResource(SysResourceEntity resource) {

        int row = resourceMapper.insert(resource);
        return row;
    }

    /**
     * 清空 sys_resource 数据库
     */
    @Override
    public void truncateResource() {
        resourceMapper.truncateResource();
    }

    /**
     * 根据角色ID查询资源编码列表
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    @Override
    public Set<String> selectResourceCodeByRoleId(Long roleId) {
        return resourceMapper.selectResourceCodeByRoleId(roleId);
    }

    @Override
    public List<SysResourceEntity> selectApiResourceList(Long userId) {
        SysResourceEntity resourceEntity = new SysResourceEntity();
        resourceEntity.setRequiredPermissionFlag(Constants.YES);
        return selectApiResourceList(resourceEntity, userId);
    }

    @Override
    public List<SysRoleAuth> selectSysRoleAuthAll() {
        return roleResourceMapper.selectList().stream()
                .map(SysRoleAuth::new).collect(Collectors.toList());
    }

    @Override
    public List<SysResourceEntity> selectApiResourceList(SysResourceEntity resource, Long userId) {
        List<SysResourceEntity> resourceList = null;
        // 管理员显示所有资源信息
        if (SysUserEntity.isAdmin(userId)) {
            resourceList = resourceMapper.selectResourceList(resource);
        } else {
            resource.getParams().put("userId", userId);
            resourceList = resourceMapper.selectResourceListByUserId(resource);
        }
        return resourceList;
    }

    @Override
    public List<Long> selectResourceListByRoleId(Long roleId) {
        return resourceMapper.selectResourceListByRoleId(roleId);
    }

    @Override
    public List<TreeSelect> buildResourceTreeSelect(List<SysResourceEntity> resources) {

        List<TreeSelect> treeSelects = new ArrayList<>();

        Map<String, List<SysResourceEntity>> map = resources.stream().collect(
                Collectors.groupingBy(SysResourceEntity::getClassName));
        long size = 0L;
        for (String key : map.keySet()) {
            String modularName = map.get(key).get(0).getModularName();
            TreeSelect treeSelect = new TreeSelect(++size, modularName, map.get(key));
            treeSelects.add(treeSelect);
        }
        return treeSelects;
    }

    @Override
    public void editRoleResource(Long roleId, Long[] resourceIds) {
        // 删除角色与api资源关联
        roleResourceMapper.deleteRoleResourceByRoleId(roleId);
        //添加角色与api资源管理
        if (resourceIds.length > 0) {
            List<SysResourceEntity> resourceEntities = resourceMapper.selectBatchIds(Arrays.asList(resourceIds));
            insertRoleMenu(roleId, resourceEntities);
        }
    }

    public int insertRoleMenu(Long roleId, List<SysResourceEntity> resourceEntities) {

        List<SysRoleResourceEntity> rrList = new ArrayList<>();

        for (SysResourceEntity resourceEntity : resourceEntities) {
            SysRoleResourceEntity rr = new SysRoleResourceEntity();
            rr.setRoleId(roleId);
            rr.setResourceCode(resourceEntity.getResourceCode());
            rrList.add(rr);
        }

        return roleResourceMapper.saveBatch(rrList);

    }

}
