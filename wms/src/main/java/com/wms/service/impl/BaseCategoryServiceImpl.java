package com.wms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.entity.BaseCategory;
import com.wms.mapper.BaseCategoryMapper;
import com.wms.service.BaseCategoryService;
import org.springframework.stereotype.Service;

@Service
public class BaseCategoryServiceImpl extends ServiceImpl<BaseCategoryMapper, BaseCategory>
                implements BaseCategoryService {
        @Override
        public boolean add(com.wms.dto.BaseCategoryAddDTO addDTO) {
                BaseCategory category = new BaseCategory();
                org.springframework.beans.BeanUtils.copyProperties(addDTO, category);

                // 自动计算层级
                if (addDTO.getParentId() == null || addDTO.getParentId() == 0) {
                        category.setParentId(0L);
                        category.setLevel(1);
                } else {
                        BaseCategory parent = this.getById(addDTO.getParentId());
                        if (parent == null) {
                                throw new RuntimeException("父分类不存在");
                        }
                        if (parent.getLevel() >= 3) {
                                throw new RuntimeException("最多只支持三级分类");
                        }
                        category.setLevel(parent.getLevel() + 1);
                }

                // 校验名称唯一性
                checkUniqueName(category.getCategoryName(), category.getParentId(), null);

                return this.save(category);
        }

        @Override
        public boolean update(com.wms.dto.BaseCategoryUpdateDTO updateDTO) {
                BaseCategory category = this.getById(updateDTO.getCategoryId());
                if (category == null) {
                        throw new RuntimeException("分类不存在");
                }

                // 业务规则校验：修改二级/三级分类时，不能修改其所在的父分类
                if (updateDTO.getParentId() != null && !updateDTO.getParentId().equals(category.getParentId())) {
                        if (category.getLevel() != 1) {
                                throw new RuntimeException("二级或三级分类不允许修改上级分类");
                        }

                        // 一级分类修改ParentId (变更为子分类)
                        BaseCategory newParent = this.getById(updateDTO.getParentId());
                        if (newParent == null) {
                                throw new RuntimeException("指定的新父分类不存在");
                        }
                        if (newParent.getLevel() >= 3) {
                                throw new RuntimeException("目标父分类层级过深");
                        }
                        category.setLevel(newParent.getLevel() + 1);
                }

                // 校验名称唯一性
                if (updateDTO.getCategoryName() != null) {
                        checkUniqueName(updateDTO.getCategoryName(), category.getParentId(), category.getCategoryId());
                }

                org.springframework.beans.BeanUtils.copyProperties(updateDTO, category);
                return this.updateById(category);
        }

        private void checkUniqueName(String name, Long parentId, Long excludeId) {
                com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BaseCategory> wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
                wrapper.eq(BaseCategory::getCategoryName, name);
                wrapper.eq(BaseCategory::getParentId, parentId);
                if (excludeId != null) {
                        wrapper.ne(BaseCategory::getCategoryId, excludeId);
                }
                if (this.count(wrapper) > 0) {
                        throw new RuntimeException("当前父分类下已存在同名分类: " + name);
                }
        }

        @Override
        @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
        public boolean delete(Long id) {
                // 递归删除所有子分类
                this.removeChildren(id);
                return this.removeById(id);
        }

        private void removeChildren(Long parentId) {
                com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BaseCategory> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
                queryWrapper.eq(BaseCategory::getParentId, parentId);
                java.util.List<BaseCategory> children = this.list(queryWrapper);
                for (BaseCategory child : children) {
                        removeChildren(child.getCategoryId());
                        this.removeById(child.getCategoryId());
                }
        }
}
