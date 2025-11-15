package com.hse.leihsy.config;

import com.hse.leihsy.model.entity.Category;
import com.hse.leihsy.model.entity.Item;
import com.hse.leihsy.model.entity.ItemStatus;
import com.hse.leihsy.repository.CategoryRepository;
import com.hse.leihsy.repository.ItemRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(CategoryRepository categoryRepo, ItemRepository itemRepo) {
        return args -> {
            // Kategorien erstellen
            Category vrCategory = new Category();
            vrCategory.setName("VR-Equipment");
            vrCategory.setDescription("Virtual Reality Equipment");
            vrCategory = categoryRepo.save(vrCategory);

            Category photoCategory = new Category();
            photoCategory.setName("Foto-Equipment");
            photoCategory.setDescription("Kameras und Zubehör");
            photoCategory = categoryRepo.save(photoCategory);

            Category itCategory = new Category();
            itCategory.setName("IT-Geräte");
            itCategory.setDescription("Computer und Peripherie");
            itCategory = categoryRepo.save(itCategory);

            // Items erstellen
            Item item1 = new Item();
            item1.setInventoryNumber("VR-001");
            item1.setName("Meta Quest 3");
            item1.setDescription("VR-Brille mit Controller");
            item1.setCategory(vrCategory);
            item1.setLocation("F01.402");
            item1.setStatus(ItemStatus.AVAILABLE);
            item1.setAccessories("Controller, Ladekabel");
            itemRepo.save(item1);

            Item item2 = new Item();
            item2.setInventoryNumber("CAM-001");
            item2.setName("Canon EOS R5");
            item2.setDescription("Profi-Kamera");
            item2.setCategory(photoCategory);
            item2.setLocation("F01.301");
            item2.setStatus(ItemStatus.AVAILABLE);
            item2.setAccessories("Objektiv 24-70mm, Speicherkarte 64GB");
            itemRepo.save(item2);

            Item item3 = new Item();
            item3.setInventoryNumber("VR-002");
            item3.setName("Meta Quest 2");
            item3.setDescription("VR-Brille älteres Modell");
            item3.setCategory(vrCategory);
            item3.setLocation("F01.402");
            item3.setStatus(ItemStatus.AVAILABLE);
            item3.setAccessories("Controller, Ladekabel, Transporttasche");
            itemRepo.save(item3);

            System.out.println("✅ Testdaten wurden geladen!");
            System.out.println(itemRepo.count() + " Items erstellt");
            System.out.println(categoryRepo.count() + " Kategorien erstellt");
        };
    }
}