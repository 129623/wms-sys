package com.wms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.entity.BaseProduct;
import com.wms.mapper.BaseProductMapper;
import com.wms.service.BaseProductService;
import org.springframework.stereotype.Service;

@Service
public class BaseProductServiceImpl extends ServiceImpl<BaseProductMapper, BaseProduct> implements BaseProductService {

    @org.springframework.beans.factory.annotation.Autowired
    private com.wms.service.BaseUnitService unitService;
    @org.springframework.beans.factory.annotation.Autowired
    private com.wms.service.BaseStorageTypeService storageTypeService;
    @org.springframework.beans.factory.annotation.Autowired
    private com.wms.service.BaseProductLabelService labelService;

    @org.springframework.beans.factory.annotation.Autowired
    private com.wms.service.BaseCategoryService categoryService;

    @Override
    public boolean add(com.wms.dto.BaseProductAddDTO addDTO) {
        BaseProduct product = new BaseProduct();
        org.springframework.beans.BeanUtils.copyProperties(addDTO, product);

        // 验证关联数据是否存在
        validateReferences(product);

        // 自动生成 SKU (如果前端没传)
        if (product.getSkuCode() == null || product.getSkuCode().isEmpty()) {
            product.setSkuCode(generateSku());
        }

        // 校验 SKU 唯一性
        if (checkSkuExists(product.getSkuCode(), null)) {
            throw new RuntimeException("SKU编码已存在: " + product.getSkuCode());
        }

        return this.save(product);
    }

    @Override
    public boolean update(com.wms.dto.BaseProductUpdateDTO updateDTO) {
        BaseProduct product = this.getById(updateDTO.getProductId());
        if (product == null) {
            throw new RuntimeException("产品不存在");
        }

        // 复制属性
        org.springframework.beans.BeanUtils.copyProperties(updateDTO, product);

        // 验证关联数据
        validateReferences(product);

        // 如果修改了SKU，需校验唯一性
        if (checkSkuExists(product.getSkuCode(), product.getProductId())) {
            throw new RuntimeException("SKU编码已存在: " + product.getSkuCode());
        }

        return this.updateById(product);
    }

    private void validateReferences(BaseProduct product) {
        if (product.getCategoryId() != null && categoryService.getById(product.getCategoryId()) == null) {
            throw new RuntimeException("指定的分类不存在");
        }
        if (product.getUnitId() != null) {
            com.wms.entity.BaseUnit unit = unitService.getById(product.getUnitId());
            if (unit == null) {
                throw new RuntimeException("指定的单位不存在");
            }
            // 如果 unit 字段为空，自动填充单位名称
            if (product.getUnit() == null || product.getUnit().trim().isEmpty()) {
                product.setUnit(unit.getUnitName());
            }
        }
        if (product.getStorageTypeId() != null && storageTypeService.getById(product.getStorageTypeId()) == null) {
            throw new RuntimeException("指定的存放类型不存在");
        }
        if (product.getLabelId() != null && labelService.getById(product.getLabelId()) == null) {
            throw new RuntimeException("指定的标签不存在");
        }
    }

    private String generateSku() {
        // 策略: PROD + 毫秒级时间戳 + 3位随机数
        String timestamp = String.valueOf(System.currentTimeMillis());
        int random = (int) (Math.random() * 900) + 100;
        return "PROD" + timestamp + random;
    }

    private boolean checkSkuExists(String sku, Long excludeId) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BaseProduct> wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(BaseProduct::getSkuCode, sku);
        if (excludeId != null) {
            wrapper.ne(BaseProduct::getProductId, excludeId);
        }
        return this.count(wrapper) > 0;
    }
}
