package com.cafe.jenika.service;

import com.cafe.jenika.model.StoreConfig;
import com.cafe.jenika.repository.StoreConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StoreConfigService {

    @Autowired
    private StoreConfigRepository storeConfigRepository;

    @Transactional
    public StoreConfig getConfig() {
        return storeConfigRepository.findById(1)
                .orElseGet(() -> {
                    StoreConfig defaultConfig = StoreConfig.builder()
                            .id(1)
                            .shopName("JENKA COFFEE SHOP")
                            .shopNamePnh("Jenka Coffee Shop")
                            .shopAddr("Địa chỉ: Số 12 Trần Thị Do - Khu phố 24 - Phường Tân Thới Hiệp - TP HCM")
                            .shopTel("Điện thoại: 0817909090 - 0827909090")
                            .shopBank("Số TK: 2050103869999 - Ngân hàng MB bank - Chủ tài khoản: Dương Văn Công")
                            .shopNotes("   - Khi mua hàng Nếu có sai lệch về hàng hoá và số lượng so với HĐBH/ phiếu giao nhận của dịch vụ vận chuyển, hãy liên hệ ngay với NVKD để được giải quyết (Chúng tôi chỉ giải quyết khiếu nại về giao nhận trong ngày Quý khách nhận được hàng).\n" +
                                    "   - Về đơn hàng: Chúng tôi chỉ giải quyết khiếu nại trong 2 ngày kể từ ngày Quý khách nhận được hàng (bao gồm các trường hợp về số lượng sản phẩm và trình trạng hàng hoá như: vỡ hỏng, móp méo, lỗi). Quý khách vui lòng cung cấp hình ảnh, video hàng hoá thực nhận cho NVKD để khiếu nại.\n" +
                                    "   - Trong trường hợp bảo hành máy, Quý khách vui lòng gửi máy về cửa hàng để kiểm tra và sửa chữa cho quý khách được thuận tiện và nhanh nhất.")
                            .shopPolicy("Nếu khách hàng muốn đổi,  trả lại máy thì phải chịu phí 30% giá trị máy.\n" +
                                    " - Sau 01 tháng thì tuỳ thuộc vào giá thị trường và độ hao mòn của máy.\n" +
                                    " - Khi trả lại máy cho nhà cung cấp thì sau 7-10 ngày sẽ hoàn trả lại tiền theo quy định trên.")
                            .shopWarranty("- Chế độ bảo hành chính hãng chỉ có hiệu lực với các sự cố do lỗi của nhà sản xuất. Nội dung bảo hành thực hiện theo chính sách bảo hành của nhà sản xuất. Các trường hợp lỗi do chập cháy, thiên tai, hoả hoạn hoặc sử dụng, bảo quản thiết bị không đúng chỉ dẫn của nhà sản xuất, do lỗi nguyên nhân chủ quan sẽ không được bảo hành.\n" +
                                    "- Các phụ kiện không được bảo hành: Vỏ ngoài, pin, các thiết bị hao mòn: Trục Socker, lưỡi dao, cối đựng, que khuấy, gioăng cao su, lưỡi ép...")
                            .shopWarrantyLimit("- Bảo hành 3-6 tháng với máy cũ và 12 tháng với máy mới.")
                            .build();
                    return storeConfigRepository.save(defaultConfig);
                });
    }

    @Transactional
    public StoreConfig saveConfig(StoreConfig updated) {
        StoreConfig existing = storeConfigRepository.findById(1)
                .orElseGet(() -> {
                    StoreConfig sc = new StoreConfig();
                    sc.setId(1);
                    return sc;
                });
        existing.setShopName(updated.getShopName());
        existing.setShopNamePnh(updated.getShopNamePnh());
        existing.setShopAddr(updated.getShopAddr());
        existing.setShopTel(updated.getShopTel());
        existing.setShopBank(updated.getShopBank());
        existing.setShopNotes(updated.getShopNotes());
        existing.setShopPolicy(updated.getShopPolicy());
        existing.setShopWarranty(updated.getShopWarranty());
        existing.setShopWarrantyLimit(updated.getShopWarrantyLimit());
        return storeConfigRepository.save(existing);
    }
}
