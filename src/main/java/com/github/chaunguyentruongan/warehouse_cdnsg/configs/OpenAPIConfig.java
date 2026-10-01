package com.github.chaunguyentruongan.warehouse_cdnsg.configs;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Quản Lý Kho - CDN SG")
                        .version("1.0.0")
                        .description("Tài liệu API cho chức năng quản lý Nhập/Xuất kho, Vật tư.")
                        .contact(new Contact()
                                .name("Admin")
                                .email("admin@admin.com")));
    }
}