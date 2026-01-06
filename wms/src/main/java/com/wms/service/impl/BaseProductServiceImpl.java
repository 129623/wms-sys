package com.wms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.entity.BaseProduct;
import com.wms.mapper.BaseProductMapper;
import com.wms.service.BaseProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional(rollbackFor = Exception.class)
    public boolean add(com.wms.dto.BaseProductAddDTO addDTO) {
        BaseProduct product = new BaseProduct();
        org.springframework.beans.BeanUtils.copyProperties(addDTO, product);

        // 验证关联数据是否存在
        validateReferences(product);

        boolean autoGenerate = (product.getSkuCode() == null || product.getSkuCode().isEmpty());

        if (autoGenerate) {
            // 1. 设置临时 SKU (TEMP-时间戳-随机数)
            // 用于绕过数据库唯一约束，确保能先保存拿到 ID
            product.setSkuCode("TEMP-" + System.currentTimeMillis() + "-" + (int) (Math.random() * 1000));
        } else {
            // 校验 SKU 唯一性
            if (checkSkuExists(product.getSkuCode(), null)) {
                throw new RuntimeException("SKU编码已存在: " + product.getSkuCode());
            }
        }

        // 2. 保存获取 ID
        boolean success = this.save(product);
        if (!success) {
            return false;
        }

        // 3. 如果是自动生成，根据 ID 更新最终 SKU
        if (autoGenerate) {
            String finalSku = generateSkuRule(product);
            product.setSkuCode(finalSku);

            // 再次校验唯一性
            if (checkSkuExists(finalSku, product.getProductId())) {
                // 极小概率冲突，抛出异常回滚
                throw new RuntimeException("生成的SKU已存在，请重试: " + finalSku);
            }
            this.updateById(product);
        }

        return true;
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

    /**
     * 生成 SKU 规则: 固定前缀(SK) + category_id(3位) + product_id(5位) + 规格hash(3位)
     */
    private String generateSkuRule(BaseProduct product) {
        // 1. Category ID (补齐3位)
        Long catId = product.getCategoryId() != null ? product.getCategoryId() : 0L;
        String catPart = String.format("%03d", catId % 1000);

        // 2. Product ID (补齐5位)
        Long prodId = product.getProductId();
        String idPart = String.format("%05d", prodId % 100000);

        // 3. Spec Hash (取绝对值后模1000，补齐3位)
        int specHash = 0;
        if (product.getSpec() != null) {
            specHash = Math.abs(product.getSpec().hashCode()) % 1000;
        }
        String specPart = String.format("%03d", specHash);

        return "SK" + catPart + idPart + specPart;
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
