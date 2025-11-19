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

            Category audioCategory = new Category();
            audioCategory.setName("Audio-Equipment");
            audioCategory.setDescription("Mikrofone und Aufnahmegeräte");
            audioCategory = categoryRepo.save(audioCategory);

            // VR Equipment
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
            item2.setInventoryNumber("VR-002");
            item2.setName("Meta Quest 2");
            item2.setDescription("VR-Brille älteres Modell");
            item2.setCategory(vrCategory);
            item2.setLocation("F01.402");
            item2.setStatus(ItemStatus.AVAILABLE);
            item2.setAccessories("Controller, Ladekabel, Transporttasche");
            itemRepo.save(item2);

            Item item3 = new Item();
            item3.setInventoryNumber("VR-003");
            item3.setName("HTC Vive Pro 2");
            item3.setDescription("High-End VR-Headset");
            item3.setCategory(vrCategory);
            item3.setLocation("F01.402");
            item3.setStatus(ItemStatus.BORROWED);
            item3.setAccessories("Base Stations, Controller, Kabel");
            itemRepo.save(item3);

            // Foto Equipment
            Item item4 = new Item();
            item4.setInventoryNumber("CAM-001");
            item4.setName("Canon EOS R5");
            item4.setDescription("Profi-Kamera");
            item4.setCategory(photoCategory);
            item4.setLocation("F01.301");
            item4.setStatus(ItemStatus.AVAILABLE);
            item4.setAccessories("Objektiv 24-70mm, Speicherkarte 64GB");
            itemRepo.save(item4);

            Item item5 = new Item();
            item5.setInventoryNumber("CAM-002");
            item5.setName("Sony A7 III");
            item5.setDescription("Vollformat Mirrorless Kamera");
            item5.setCategory(photoCategory);
            item5.setLocation("F01.301");
            item5.setStatus(ItemStatus.AVAILABLE);
            item5.setAccessories("Objektiv 28-70mm, 2x Akku");
            itemRepo.save(item5);

            Item item6 = new Item();
            item6.setInventoryNumber("CAM-003");
            item6.setName("DJI Mavic 3 Pro");
            item6.setDescription("Professionelle Drohne mit Kamera");
            item6.setCategory(photoCategory);
            item6.setLocation("F01.301");
            item6.setStatus(ItemStatus.MAINTENANCE);
            item6.setAccessories("3x Akku, Fernsteuerung, Transportkoffer");
            itemRepo.save(item6);

            // IT-Geräte
            Item item7 = new Item();
            item7.setInventoryNumber("IT-001");
            item7.setName("MacBook Pro 16\"");
            item7.setDescription("M3 Pro, 36GB RAM, 1TB SSD");
            item7.setCategory(itCategory);
            item7.setLocation("F01.205");
            item7.setStatus(ItemStatus.AVAILABLE);
            item7.setAccessories("Ladekabel USB-C, Tasche");
            itemRepo.save(item7);

            Item item8 = new Item();
            item8.setInventoryNumber("IT-002");
            item8.setName("iPad Pro 12.9\"");
            item8.setDescription("M2 Chip, 256GB, mit Apple Pencil");
            item8.setCategory(itCategory);
            item8.setLocation("F01.205");
            item8.setStatus(ItemStatus.BORROWED);
            item8.setAccessories("Apple Pencil 2, Magic Keyboard");
            itemRepo.save(item8);

            // Audio Equipment
            Item item9 = new Item();
            item9.setInventoryNumber("AUD-001");
            item9.setName("Rode NT1-A");
            item9.setDescription("Kondensator-Mikrofon");
            item9.setCategory(audioCategory);
            item9.setLocation("F01.310");
            item9.setStatus(ItemStatus.AVAILABLE);
            item9.setAccessories("Spinne, Popschutz, XLR-Kabel");
            itemRepo.save(item9);

            Item item10 = new Item();
            item10.setInventoryNumber("AUD-002");
            item10.setName("Zoom H6");
            item10.setDescription("Mobiler Audio-Recorder");
            item10.setCategory(audioCategory);
            item10.setLocation("F01.310");
            item10.setStatus(ItemStatus.AVAILABLE);
            item10.setAccessories("4x AA Batterien, SD-Karte 64GB");
            itemRepo.save(item10);

            System.out.println("Testdaten wurden geladen!");
            System.out.println(itemRepo.count() + " Items erstellt");
            System.out.println(categoryRepo.count() + " Kategorien erstellt");
        };
    }
}