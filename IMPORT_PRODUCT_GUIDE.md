# Hướng Dẫn Import Sản Phẩm từ File CSV/Excel

## Tính Năng
- Import nhiều sản phẩm cùng lúc từ file CSV hoặc Excel
- Hỗ trợ nhiều ảnh trên cùng một sản phẩm
- Hỗ trợ nhiều biến thể (variant) cho mỗi sản phẩm

## Định Dạng File

### Cột Yêu Cầu
| Cột | Kiểu | Mô Tả | Bắt Buộc |
|-----|------|-------|----------|
| name | String | Tên sản phẩm | ✓ |
| brand | String | Thương hiệu | ✓ |
| gender | String | Giới tính (Unisex, Male, Female) | ✓ |
| concentration | String | Nồng độ (EDP, EDT, etc.) | ✗ |
| releaseYear | Integer | Năm phát hành | ✗ |
| description | String | Mô tả sản phẩm | ✗ |
| normalizedKey | String | Khóa duy nhất (tên chuẩn hoá) | ✓ |
| price | Decimal | Giá gốc sản phẩm | ✗ |
| sku | String | Mã SKU (duy nhất cho mỗi biến thể) | ✓ |
| volume | Integer | Dung tích (ml) | ✗ |
| variantName | String | Tên biến thể (VD: 10ml) | ✓ |
| variantPrice | Decimal | Giá bán của biến thể | ✓ |
| stockQuantity | Integer | Số lượng tồn kho | ✓ |
| isActive | Boolean | Trạng thái hoạt động (true/false) | ✗ |
| secureUrl | String | URL ảnh từ Cloudinary | ✗ |
| altText | String | Văn bản thay thế cho ảnh | ✗ |
| isMain | Boolean | Ảnh chính (true/false) | ✗ |
| sortOrder | Integer | Thứ tự sắp xếp ảnh | ✗ |

## Cách Sử Dụng

### Cấu Trúc Dữ Liệu

**Nhiều Ảnh cho Cùng Một Biến Thể:**
- Mỗi dòng có thể đại diện một ảnh của cùng variant
- Dùng cùng SKU và product info
- Tăng sortOrder cho mỗi ảnh tiếp theo

Ví dụ:
```
name | brand | sku | variantName | secureUrl | sortOrder
Tom Ford Ombre | Tom Ford | SKU-001 | 10ml | url1.jpg | 0
Tom Ford Ombre | Tom Ford | SKU-001 | 10ml | url2.jpg | 1
```

**Nhiều Biến Thể cho Cùng Một Sản Phẩm:**
- Mỗi dòng có thể là một variant khác nhau
- Dùng cùng normalizedKey và product info
- Dùng SKU khác nhau

Ví dụ:
```
normalizedKey | sku | variantName | variantPrice | volume
tom-ford-ombre | SKU-001 | 10ml | 100000 | 10
tom-ford-ombre | SKU-002 | 50ml | 350000 | 50
tom-ford-ombre | SKU-003 | 100ml | 600000 | 100
```

### Endpoint

```
POST /pub/api/v1/file/products/import
Content-Type: multipart/form-data

Body:
- file: [CSV hoặc Excel file]
```

### Response

```json
{
  "totalRows": 10,
  "successRows": 9,
  "failedRows": 1
}
```

## Lưu Ý

- **normalizedKey** không được để trống (dùng để nhóm sản phẩm)
- **sku** không được để trống (dùng để nhóm biến thể)
- **secureUrl** nên từ Cloudinary để đảm bảo tính ổn định
- Các dòng không có normalizedKey hoặc sku sẽ bị loại bỏ (counted as failed)
- Mỗi variant phải có ít nhất một row với thông tin biến thể hợp lệ

## Ví Dụ Đầy Đủ

### Sản Phẩm: Tom Ford Ombre Leather
- 3 biến thể (10ml, 50ml, 100ml)
- Mỗi biến thể có 2 ảnh

CSV Structure:
```
name,brand,normalizedKey,sku,volume,variantName,variantPrice,stockQuantity,secureUrl,altText,isMain,sortOrder
Tom Ford Ombre,Tom Ford,tom-ford-ombre,SKU-001,10,10ml,100000,50,url1.jpg,Main image,true,0
Tom Ford Ombre,Tom Ford,tom-ford-ombre,SKU-001,10,10ml,100000,50,url2.jpg,Side image,false,1
Tom Ford Ombre,Tom Ford,tom-ford-ombre,SKU-002,50,50ml,350000,30,url3.jpg,Main image,true,0
Tom Ford Ombre,Tom Ford,tom-ford-ombre,SKU-002,50,50ml,350000,30,url4.jpg,Side image,false,1
Tom Ford Ombre,Tom Ford,tom-ford-ombre,SKU-003,100,100ml,600000,20,url5.jpg,Main image,true,0
Tom Ford Ombre,Tom Ford,tom-ford-ombre,SKU-003,100,100ml,600000,20,url6.jpg,Side image,false,1
```

Kết quả:
- 1 sản phẩm (Tom Ford Ombre)
- 3 biến thể
- 6 ảnh (2 cho mỗi biến thể)
