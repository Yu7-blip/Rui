package com.auth.config;

import com.auth.entity.User;
import com.auth.entity.Product;
import com.auth.entity.Region;
import com.auth.entity.RegionProduct;
import com.auth.repository.UserRepository;
import com.auth.repository.ProductRepository;
import com.auth.repository.RegionRepository;
import com.auth.repository.RegionProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private RegionProductRepository regionProductRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 1. 首先确保地域表有数据
        initializeRegions();

        // 2. 初始化商品数据
        if (productRepository.count() == 0) {
            int imageId = 1; // 图片编号从1开始

            // 天气特饮类产品 (图片ID: 1-10)
            Product[] weatherProducts = {
                    createProduct("晴天蜜桃冰茶", "清爽蜜桃搭配晴天气息，阳光般温暖", 22.00, "weather", "🍑", "晴天,果茶,清爽", imageId++),
                    createProduct("雨天暖姜奶茶", "温暖姜茶驱散雨天寒意，暖心暖胃", 20.00, "weather", "☕", "雨天,暖饮,姜茶", imageId++),
                    createProduct("雪天巧克力热饮", "浓郁巧克力温暖整个雪天，甜蜜治愈", 26.00, "weather", "🍫", "雪天,热饮,巧克力", imageId++),
                    createProduct("夏日芒果冰沙", "新鲜芒果冰沙，清凉解暑", 24.00, "weather", "🥭", "夏日,冰沙,芒果", imageId++),
                    createProduct("冬日红枣桂圆", "红枣桂圆暖身茶，冬日必备", 18.00, "weather", "❤️", "冬日,暖饮,养生", imageId++),
                    createProduct("春风茉莉花茶", "清香茉莉花茶，春风般柔和", 16.00, "weather", "🌼", "春风,花茶,清香", imageId++),
                    createProduct("秋日桂花乌龙", "桂花香乌龙茶，秋日浪漫", 19.00, "weather", "🍂", "秋日,桂花,乌龙", imageId++),
                    createProduct("热带风暴果茶", "多种热带水果混合，风暴般强烈口感", 28.00, "weather", "🌪️", "热带,混合,果茶", imageId++),
                    createProduct("阳光橙子绿茶", "新鲜橙子搭配绿茶，阳光活力", 21.00, "weather", "🍊", "阳光,橙子,绿茶", imageId++),
                    createProduct("雾霾清肺茶", "罗汉果梨汤，雾霾天清肺佳品", 17.00, "weather", "🌫️", "雾霾,清肺,养生", imageId++)
            };

            // 节日限定类产品 (图片ID: 11-20)
            Product[] festivalProducts = {
                    createProduct("圣诞限定奶茶", "圣诞特调暖心奶茶，姜饼人造型", 30.00, "festival", "🎄", "圣诞,限定,暖心", imageId++),
                    createProduct("春节福气茶", "春节特饮，福气满满，年味十足", 28.00, "festival", "🧧", "春节,福气,年味", imageId++),
                    createProduct("情人节甜蜜", "浪漫情人节限定饮品，爱心造型", 32.00, "festival", "❤️", "情人节,浪漫,爱心", imageId++),
                    createProduct("万圣节南瓜拿铁", "南瓜风味拿铁，万圣节特调", 26.00, "festival", "🎃", "万圣节,南瓜,拿铁", imageId++),
                    createProduct("中秋月饼奶茶", "月饼风味奶茶，中秋限定", 29.00, "festival", "🥮", "中秋,月饼,限定", imageId++),
                    createProduct("七夕星空茶", "梦幻星空渐变，七夕浪漫特饮", 35.00, "festival", "🌌", "七夕,星空,浪漫", imageId++),
                    createProduct("端午粽子奶茶", "粽子风味创意奶茶，端午特色", 27.00, "festival", "🎋", "端午,粽子,创意", imageId++),
                    createProduct("元旦迎新茶", "新年特调，迎接新的一年", 25.00, "festival", "🎉", "元旦,迎新,新年", imageId++),
                    createProduct("儿童节彩虹茶", "七彩彩虹分层，童趣满满", 23.00, "festival", "🌈", "儿童节,彩虹,童趣", imageId++),
                    createProduct("感恩节南瓜派", "南瓜派风味奶茶，感恩温暖", 26.00, "festival", "🦃", "感恩节,南瓜,温暖", imageId++)
            };

            // 特色创意类产品 (图片ID: 21-30)
            Product[] specialProducts = {
                    createProduct("星空奶茶", "梦幻星空渐变色彩，颜值爆表", 35.00, "special", "🌌", "特色,星空,渐变", imageId++),
                    createProduct("泡泡浴奶茶", "创意泡泡浴造型奶茶，趣味十足", 38.00, "special", "🛁", "特色,创意,泡泡", imageId++),
                    createProduct("火山熔岩", "黑糖熔岩效果，视觉冲击", 32.00, "special", "🌋", "特色,熔岩,黑糖", imageId++),
                    createProduct("海洋之心", "蓝色海洋色调，清爽治愈", 29.00, "special", "🌊", "特色,海洋,蓝色", imageId++),
                    createProduct("森林迷雾", "抹茶与奶盖的层次，如森林晨雾", 27.00, "special", "🌲", "特色,抹茶,层次", imageId++),
                    createProduct("银河系特调", "星空珍珠与蝶豆花，银河般美丽", 36.00, "special", "🌠", "特色,银河,蝶豆花", imageId++),
                    createProduct("彩虹云朵", "棉花糖云顶，彩虹糖装饰", 31.00, "special", "☁️", "特色,彩虹,棉花糖", imageId++),
                    createProduct("魔法药水", "变色茶饮，魔法般神奇", 34.00, "special", "🧪", "特色,魔法,变色", imageId++),
                    createProduct("钻石冰晶", "水晶冻与碎冰，钻石般闪耀", 33.00, "special", "💎", "特色,水晶,闪耀", imageId++),
                    createProduct("火焰山", "辣椒巧克力特调，火辣体验", 30.00, "special", "🔥", "特色,辣味,创意", imageId++)
            };

            // 奶茶系列产品 (图片ID: 31-45)
            Product[] milkteaProducts = {
                    createProduct("经典珍珠奶茶", "香浓奶茶搭配Q弹珍珠，甜度适中", 18.00, "milktea", "🧋", "奶茶,珍珠,经典", imageId++),
                    createProduct("芋圆奶茶", "香糯芋圆搭配醇香奶茶", 20.00, "milktea", "🟣", "奶茶,芋圆,香糯", imageId++),
                    createProduct("红豆奶茶", "香甜红豆与奶茶的经典组合", 19.00, "milktea", "❤️", "奶茶,红豆,香甜", imageId++),
                    createProduct("布丁奶茶", "滑嫩布丁与香浓奶茶", 21.00, "milktea", "🍮", "奶茶,布丁,滑嫩", imageId++),
                    createProduct("椰果奶茶", "Q弹椰果，清爽口感", 18.00, "milktea", "🥥", "奶茶,椰果,清爽", imageId++),
                    createProduct("仙草冻奶茶", "清凉仙草冻，消暑佳品", 20.00, "milktea", "🍀", "奶茶,仙草,清凉", imageId++),
                    createProduct("燕麦奶茶", "健康燕麦，营养美味", 22.00, "milktea", "🌾", "奶茶,燕麦,健康", imageId++),
                    createProduct("黑糖珍珠奶茶", "古法黑糖，焦香浓郁", 23.00, "milktea", "🖤", "奶茶,黑糖,焦香", imageId++),
                    createProduct("焦糖奶茶", "焦糖风味，香甜不腻", 19.00, "milktea", "🍯", "奶茶,焦糖,香甜", imageId++),
                    createProduct("丝袜奶茶", "港式经典，丝滑口感", 24.00, "milktea", "🧦", "奶茶,港式,丝滑", imageId++),
                    createProduct("抹茶拿铁", "日式抹茶搭配香醇拿铁", 25.00, "milktea", "🍵", "奶茶,抹茶,拿铁", imageId++),
                    createProduct("巧克力奶茶", "浓郁巧克力风味奶茶", 22.00, "milktea", "🍫", "奶茶,巧克力,浓郁", imageId++),
                    createProduct("芝士奶盖红茶", "红茶底搭配绵密芝士奶盖", 26.00, "milktea", "🧀", "奶茶,芝士,奶盖", imageId++),
                    createProduct("伯爵奶茶", "英式伯爵茶风味", 21.00, "milktea", "👑", "奶茶,伯爵,英式", imageId++),
                    createProduct("阿华田奶茶", "经典阿华田风味", 23.00, "milktea", "🥤", "奶茶,阿华田,经典", imageId++)
            };

            // 水果茶类 (图片ID: 46-55)
            Product[] fruitProducts = {
                    createProduct("草莓果茶", "新鲜草莓搭配清茶，果香浓郁", 24.00, "fruit", "🍓", "果茶,草莓,新鲜", imageId++),
                    createProduct("芒果冰沙", "香甜芒果制成冰沙，清凉解渴", 26.00, "fruit", "🥭", "果茶,冰沙,芒果", imageId++),
                    createProduct("百香果绿茶", "百香果酸甜，绿茶清香", 19.00, "fruit", "🍋", "果茶,百香果,绿茶", imageId++),
                    createProduct("葡萄多多", "巨峰葡萄果肉，多汁美味", 25.00, "fruit", "🍇", "果茶,葡萄,多肉", imageId++),
                    createProduct("桃子乌龙", "白桃乌龙，清甜可口", 22.00, "fruit", "🍑", "果茶,桃子,乌龙", imageId++),
                    createProduct("柠檬绿茶", "清新柠檬，酸甜解腻", 17.00, "fruit", "🍋", "果茶,柠檬,绿茶", imageId++),
                    createProduct("西瓜汁", "新鲜西瓜榨汁，夏日必备", 16.00, "fruit", "🍉", "果茶,西瓜,解暑", imageId++),
                    createProduct("菠萝冰茶", "热带菠萝，冰爽口感", 21.00, "fruit", "🍍", "果茶,菠萝,热带", imageId++),
                    createProduct("蓝莓酸奶", "蓝莓果粒与酸奶的完美结合", 23.00, "fruit", "🫐", "果茶,蓝莓,酸奶", imageId++),
                    createProduct("石榴气泡", "石榴果粒搭配气泡水，清爽刺激", 20.00, "fruit", "🌺", "果茶,石榴,气泡", imageId++)
            };

            // 经典茶饮类 (图片ID: 56-60)
            Product[] classicProducts = {
                    createProduct("四季春茶", "清香四季春茶，茶味醇厚", 15.00, "classic", "🍵", "经典,四季春,清香", imageId++),
                    createProduct("铁观音", "传统铁观音，回甘持久", 16.00, "classic", "🌿", "经典,铁观音,传统", imageId++),
                    createProduct("龙井绿茶", "西湖龙井，清香甘醇", 18.00, "classic", "🐉", "经典,龙井,绿茶", imageId++),
                    createProduct("普洱熟茶", "陈年普洱，醇厚顺滑", 20.00, "classic", "🍂", "经典,普洱,陈年", imageId++),
                    createProduct("大红袍", "岩茶之王，香气独特", 22.00, "classic", "🏔️", "经典,大红袍,岩茶", imageId)
            };

// 在现有产品初始化代码后面添加（在 saveProductArray 调用之前）：

// 特色茶叶类产品 (图片ID: 61-90)
            Product[] teaProducts = {
                    // 西南地区茶叶
                    createProduct("贵州白茶", "贵州高山白茶，汤色清澈，滋味鲜爽甘甜，具有独特的毫香", 45.00, "tea", "🍃", "白茶,高山茶,贵州", 61),
                    createProduct("云南普洱茶", "陈年普洱，香气独特，越陈越香，具有降脂减肥功效", 68.00, "tea", "🫖", "普洱茶,黑茶,云南", 62),
                    createProduct("四川蒙顶茶", "蒙顶甘露，历史名茶，香气鲜嫩持久，汤色碧清微黄", 52.00, "tea", "🍵", "绿茶,蒙顶茶,四川", 63),

                    // 华东地区茶叶
                    createProduct("杭州西湖龙井", "中国十大名茶之一，色绿、香郁、味甘、形美，被誉为绿茶皇后", 88.00, "tea", "🐉", "龙井茶,绿茶,浙江", 64),
                    createProduct("福建铁观音", "乌龙茶代表，观音韵明显，回甘带蜜，七泡有余香", 75.00, "tea", "🌿", "铁观音,乌龙茶,福建", 65),
                    createProduct("安徽黄山毛峰", "黄山名茶，形似雀舌，白毫显露，香气如兰", 65.00, "tea", "🏔️", "毛峰茶,绿茶,安徽", 66),

                    // 华南地区茶叶
                    createProduct("广东凉茶", "岭南传统草本茶饮，清热祛湿，适应炎热潮湿气候", 25.00, "tea", "🌿", "凉茶,草本茶,广东", 67),
                    createProduct("广西六堡茶", "黑茶代表，陈香明显，有槟榔香味，适合陈放", 58.00, "tea", "🫖", "六堡茶,黑茶,广西", 68),
                    createProduct("海南鹧鸪茶", "海南特色茶叶，清热消暑，茶香独特，野生生长", 42.00, "tea", "🐦", "鹧鸪茶,野生茶,海南", 69),

                    // 华北地区茶叶
                    createProduct("北京茉莉花茶", "京味代表，茉莉花香浓郁，是北京人最爱的日常茶饮", 35.00, "tea", "🌸", "茉莉花茶,花茶,北京", 70),
                    createProduct("山西沙棘茶", "山西特产，富含维C，酸甜可口，具有保健功效", 38.00, "tea", "🌱", "沙棘茶,保健茶,山西", 71),
                    createProduct("蒙古奶茶", "草原风味奶茶，奶香浓郁，咸香可口，是牧民日常饮品", 28.00, "tea", "🥛", "奶茶,咸奶茶,蒙古", 72),

                    // 东北地区茶叶
                    createProduct("长白山人参茶", "东北特产，人参入茶，滋补养生，提神醒脑", 95.00, "tea", "🌱", "人参茶,保健茶,吉林", 73),
                    createProduct("东北松子茶", "松子香气独特，口感醇厚，富含不饱和脂肪酸", 48.00, "tea", "🌰", "松子茶,坚果茶,东北", 74),
                    createProduct("黑森林红茶", "东北地区改良红茶，茶性温和，适合寒冷气候", 55.00, "tea", "🌳", "红茶,东北红茶,辽宁", 75),

                    // 西北地区茶叶
                    createProduct("宁夏枸杞茶", "宁夏特产枸杞制成，明目养肝，带有天然甜味", 45.00, "tea", "🔴", "枸杞茶,保健茶,宁夏", 76),
                    createProduct("新疆玫瑰花茶", "玫瑰花香气浓郁，美容养颜，是新疆特色花茶", 42.00, "tea", "🌹", "玫瑰花茶,花茶,新疆", 77),
                    createProduct("陕西茯砖茶", "古老黑茶，金花茂盛，陈香明显，助消化", 62.00, "tea", "🧱", "茯砖茶,黑茶,陕西", 78),

                    // 华中地区茶叶
                    createProduct("湖北恩施玉露", "蒸青绿茶，茶汤清澈，鲜爽回甘，是中国传统蒸青茶", 78.00, "tea", "💚", "玉露茶,绿茶,湖北", 79),
                    createProduct("湖南君山银针", "黄茶珍品，三起三落，茶舞动人，香气清纯", 85.00, "tea", "🟡", "银针茶,黄茶,湖南", 80),
                    createProduct("河南信阳毛尖", "中国名茶，细圆光直，白毫显露，滋味醇厚", 72.00, "tea", "🍃", "毛尖茶,绿茶,河南", 81),

                    // 东南地区茶叶
                    createProduct("台湾高山茶", "阿里山高山乌龙，高山韵味，清香持久，喉韵甘甜", 92.00, "tea", "⛰️", "高山茶,乌龙茶,台湾", 82),
                    createProduct("港式奶茶", "茶味浓郁，丝滑顺口，港式经典，使用拼配茶", 32.00, "tea", "🧋", "奶茶,港式奶茶,香港", 83),
                    createProduct("澳门杏仁茶", "杏仁香气，口感细腻，传统甜品茶饮，滋补养生", 38.00, "tea", "🥜", "杏仁茶,甜品茶,澳门", 84)
            };

            // 保存所有产品
            saveProductArray(weatherProducts, "天气特饮");
            saveProductArray(festivalProducts, "节日限定");
            saveProductArray(specialProducts, "特色创意");
            saveProductArray(milkteaProducts, "奶茶系列");
            saveProductArray(fruitProducts, "水果茶饮");
            saveProductArray(classicProducts, "经典茶饮");
            // 在保存其他产品后保存茶叶产品
            saveProductArray(teaProducts, "特色茶叶");


            System.out.println("✅ 总共初始化了 84 个商品数据！");
            System.out.println("🌤️  天气特饮: 10 款 (图片: 1-10.jpg)");
            System.out.println("🎉  节日限定: 10 款 (图片: 11-20.jpg)");
            System.out.println("✨  特色创意: 10 款 (图片: 21-30.jpg)");
            System.out.println("🧋  奶茶系列: 15 款 (图片: 31-45.jpg)");
            System.out.println("🍓  水果茶饮: 10 款 (图片: 46-55.jpg)");
            System.out.println("🍵  经典茶饮: 5 款 (图片: 56-60.jpg)");
            System.out.println("🍃 特色茶叶: 24 款 (图片: 61-84.jpg)");
        }

        // 3. 初始化地域-产品关联数据
        initializeRegionProducts();
    }

    /**
     * 初始化地域数据
     */
    private void initializeRegions() {
        if (regionRepository.count() == 0) {
            System.out.println("🌍 初始化地域数据...");

            Region[] regions = {
                    createRegion("northeast", "东北地区", "❄️", "黑龙江,吉林,辽宁", "抗寒暖身系列", "寒冷干燥", "暖身,抗寒,高热量"),
                    createRegion("north", "华北地区", "🏙️", "北京,天津,河北,山西,内蒙古", "京味茶饮系列", "四季分明", "经典,传统,文化"),
                    createRegion("northwest", "西北地区", "🏜️", "陕西,甘肃,青海,宁夏,新疆", "草原奶茶系列", "干燥少雨", "浓郁,奶香,草原"),
                    createRegion("southwest", "西南地区", "🏔️", "四川,云南,贵州,重庆,西藏", "普洱茶系列", "温暖湿润", "普洱茶,香醇,山地"),
                    createRegion("central", "华中地区", "🌉", "河南,湖北,湖南,江西", "绿茶系列", "温和湿润", "清新,绿茶,自然"),
                    createRegion("east", "华东地区", "🏯", "上海,江苏,浙江,安徽,福建,山东", "功夫茶系列", "温和多雨", "精致,功夫茶,细腻"),
                    createRegion("south", "华南地区", "🌴", "广东,广西,海南", "凉茶系列", "炎热潮湿", "清凉,消暑,草本"),
                    createRegion("southeast", "东南地区", "🌊", "台湾,香港,澳门", "珍珠奶茶系列", "亚热带", "创意,流行,珍珠")
            };

            for (Region region : regions) {
                regionRepository.save(region);
            }

            System.out.println("✅ 初始化了 " + regions.length + " 个地域数据");
        } else {
            System.out.println("✅ 地域数据已存在 (" + regionRepository.count() + " 个)");
        }
    }

    /**
     * 初始化地域-产品关联数据
     */
    private void initializeRegionProducts() {
        if (regionProductRepository.count() == 0) {
            System.out.println("🔗 初始化地域-产品关联数据...");

            List<Region> regions = regionRepository.findAll();
            List<Product> products = productRepository.findAll();

            if (regions.isEmpty() || products.isEmpty()) {
                System.out.println("❌ 无法初始化：地域或产品数据为空");
                return;
            }

            int totalAssigned = 0;

            // 为每个地域分配2-3个产品
            for (Region region : regions) {
                int productsToAssign = 2 + (region.getId().intValue() % 2); // 2或3个

                for (int i = 0; i < productsToAssign && i < products.size(); i++) {
                    Product product = products.get((region.getId().intValue() * 3 + i) % products.size());

                    // 创建关联
                    RegionProduct rp = new RegionProduct();
                    rp.setRegion(region);
                    rp.setProduct(product);
                    rp.setIsFeatured(true);
                    rp.setPopularityScore(80 + (i * 10)); // 80, 90, 100
                    rp.setRecommendReason(getRecommendReason(region, product));
                    rp.setLocalName(getLocalName(region, product));
                    rp.setSeasonalMonth(getSeasonalMonth(region));
                    rp.setCreatedAt(new Date());
                    rp.setUpdatedAt(new Date());

                    regionProductRepository.save(rp);
                    totalAssigned++;

                    System.out.println("   📍 " + region.getName() + " ← " + product.getName());
                }
            }

            System.out.println("✅ 成功创建 " + totalAssigned + " 个地域-产品关联");
        } else {
            System.out.println("✅ 地域-产品关联数据已存在 (" + regionProductRepository.count() + " 条)");
        }
    }

    /**
     * 创建地域对象
     */
    private Region createRegion(String code, String name, String icon, String provinces,
                                String specialty, String climate, String tags) {
        Region region = new Region();
        region.setCode(code);
        region.setName(name);
        region.setIcon(icon);
        region.setCoverProvinces(provinces);
        region.setSpecialtyDesc(specialty);
        region.setClimateFeature(climate);
        region.setRecommendTags(tags);
        region.setIsActive(true);
        return region;
    }

    private void saveProductArray(Product[] products, String categoryName) {
        for (Product product : products) {
            productRepository.save(product);
        }
        System.out.println("✅ " + categoryName + " - " + products.length + " 款产品");
    }

    // 修改后的方法，添加图片URL参数
    private Product createProduct(String name, String description, double price,
                                  String category, String emoji, String tags, int imageId) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setCategory(category);
        product.setEmoji(emoji);
        product.setTags(tags);
        product.setAvailable(true);
        // 设置图片路径，对应 /images/1.jpg, /images/2.jpg 等
        product.setImageUrl("/images/" + imageId + ".png");
        return product;
    }

    /**
     * 获取推荐理由
     */
    private String getRecommendReason(Region region, Product product) {
        String base = region.getName() + "特色推荐，";

        if (product.getCategory() != null) {
            switch (product.getCategory()) {
                case "milktea":
                    return base + "经典奶茶适合当地口味";
                case "fruit":
                    return base + "新鲜水果茶健康美味";
                case "weather":
                    return base + "根据当地气候特别调配";
                case "special":
                    return base + "创意饮品独具风味";
                case "festival":
                    return base + "节日氛围浓厚";
                case "classic":
                    return base + "传统茶饮文化的体现";
                default:
                    return base + "口感独特值得一试";
            }
        }

        return base + "深受当地人喜爱";
    }

    /**
     * 获取当地名称
     */
    private String getLocalName(Region region, Product product) {
        String prefix = "";

        switch (region.getCode()) {
            case "north": prefix = "京味"; break;
            case "northeast": prefix = "东北"; break;
            case "northwest": prefix = "陕甘"; break;
            case "southwest": prefix = "川滇"; break;
            case "south": prefix = "粤式"; break;
            case "east": prefix = "沪上"; break;
            case "central": prefix = "华中"; break;
            case "southeast": prefix = "港澳台"; break;
            default: prefix = "地方";
        }

        // 取产品名前2-3个字
        String name = product.getName();
        if (name.length() >= 3) {
            return prefix + name.substring(0, 3);
        } else {
            return prefix + name;
        }
    }

    /**
     * 获取季节性月份
     */
    private String getSeasonalMonth(Region region) {
        switch (region.getCode()) {
            case "north":
            case "northeast":
                return "10,11,12,1,2"; // 秋冬季节
            case "south":
                return "4,5,6,7,8,9"; // 春夏季节
            case "southwest":
                return "3,4,5,9,10"; // 春秋季节
            default:
                return "1,2,3,4,5,6,7,8,9,10,11,12"; // 全年
        }
    }
}