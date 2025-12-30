package com.wms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.entity.BaseProductLabel;
import com.wms.mapper.BaseProductLabelMapper;
import com.wms.service.BaseProductLabelService;
import org.springframework.stereotype.Service;

@Service
public class BaseProductLabelServiceImpl extends ServiceImpl<BaseProductLabelMapper, BaseProductLabel>
                implements BaseProductLabelService {

        @Override
        public boolean save(BaseProductLabel entity) {
                checkUnique(entity.getLabelCode(), entity.getLabelName(), null);
                return super.save(entity);
        }

        @Override
        public boolean updateById(BaseProductLabel entity) {
                checkUnique(entity.getLabelCode(), entity.getLabelName(), entity.getLabelId());
                return super.updateById(entity);
        }

        private void checkUnique(String code, String name, Long excludeId) {
                if (code != null) {
                        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BaseProductLabel> wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
                        wrapper.eq(BaseProductLabel::getLabelCode, code);
                        if (excludeId != null) {
                                wrapper.ne(BaseProductLabel::getLabelId, excludeId);
                        }
                        if (this.count(wrapper) > 0) {
                                throw new RuntimeException("标签编码已存在: " + code);
                        }
                }

                if (name != null) {
                        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BaseProductLabel> wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
                        wrapper.eq(BaseProductLabel::getLabelName, name);
                        if (excludeId != null) {
                                wrapper.ne(BaseProductLabel::getLabelId, excludeId);
                        }
                        if (this.count(wrapper) > 0) {
                                throw new RuntimeException("标签名称已存在: " + name);
                        }
                }
        }
}
