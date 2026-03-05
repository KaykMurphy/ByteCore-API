package util;

import com.byteCore.demo.dto.request.ProductCreateDTO;
import com.byteCore.demo.dto.request.ProductUpdateDTO;
import com.byteCore.demo.enums.ProductType;

import java.math.BigDecimal;

public class TestDataFactory {

    public static ProductCreateDTO validProductCreateDTO() {
        return new ProductCreateDTO(
                "Title",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }


    public static ProductUpdateDTO  validProductUpdateDTO() {
        return new ProductUpdateDTO(
                "Novo título",
                "Nova descrição",
                "img.png",
                true,
                ProductType.EDUCATION,
                new BigDecimal("100.00"),
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }
}
