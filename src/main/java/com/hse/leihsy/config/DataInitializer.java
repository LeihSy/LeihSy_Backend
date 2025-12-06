package com.hse.leihsy.config;

import com.hse.leihsy.model.entity.*;
import com.hse.leihsy.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;

/**
 * DataInitializer - Laedt Testdaten beim Start
 * NUR AKTIV IM DEV-PROFIL
 */
@Configuration
public class DataInitializer {

    @Bean
    @Profile("dev")
    public CommandLineRunner loadTestData(
            CategoryRepository categoryRepo,
            LocationRepository locationRepo,
            ProductRepository productRepo,
            ItemRepository itemRepo,
            UserRepository userRepo
    ) {
        return args -> {
            System.out.println("\n========================================");
            System.out.println(" DEV-MODUS: Lade Testdaten...");
            System.out.println("========================================\n");

            // ==========================================
            // 1. Kategorien erstellen
            // ==========================================
            Category catVR = new Category("VR-Equipment");
            Category catPhoto = new Category("Foto-Equipment");
            Category catAudio = new Category("Audio-Equipment");
            Category catIT = new Category("IT-Geraete");

            categoryRepo.save(catVR);
            categoryRepo.save(catPhoto);
            categoryRepo.save(catAudio);
            categoryRepo.save(catIT);

            System.out.println(categoryRepo.count() + " Kategorien erstellt");

            // ==========================================
            // 2. Locations erstellen
            // ==========================================
            Location locKEIM = new Location("F01.402 (KEIM)");
            Location locBib = new Location("Bibliothek Flandernstrasse");
            Location locVRLab = new Location("VR-Labor F01.403");

            locationRepo.save(locKEIM);
            locationRepo.save(locBib);
            locationRepo.save(locVRLab);

            System.out.println(locationRepo.count() + " Locations erstellt");

            // ==========================================
            // 3. Test-User erstellen (Verleiher)
            // ==========================================
            User verleiherChristian = new User("keycloak-id-christian", "Christian Haas");
            verleiherChristian.setBudget(BigDecimal.ZERO);

            User verleiherAndreas = new User("keycloak-id-andreas", "Andreas Heinrich");
            verleiherAndreas.setBudget(BigDecimal.ZERO);

            User studentMax = new User("keycloak-id-max", "Max Mustermann");
            studentMax.setBudget(new BigDecimal("100.00"));

            userRepo.save(verleiherChristian);
            userRepo.save(verleiherAndreas);
            userRepo.save(studentMax);

            System.out.println(userRepo.count() + " User erstellt");

            // ==========================================
            // 4. Products erstellen
            // ==========================================

            // VR Products
            Product metaQuest3 = new Product();
            metaQuest3.setName("Meta Quest 3");
            metaQuest3.setDescription("Die Meta Quest 3 ist ein Mixed-Reality-Headset mit hochaufloesendem Display und verbessertem Tracking.");
            metaQuest3.setCategory(catVR);
            metaQuest3.setLocation(locVRLab);
            metaQuest3.setExpiryDate(14);
            metaQuest3.setPrice(new BigDecimal("5.00"));
            metaQuest3.setAccessories("[\"2x Controller\", \"Ladekabel USB-C\", \"Tragetasche\"]");

            Product htcVive = new Product();
            htcVive.setName("HTC Vive Pro 2");
            htcVive.setDescription("Professionelles VR-Headset mit 5K-Aufloesung und praezisem Tracking.");
            htcVive.setCategory(catVR);
            htcVive.setLocation(locVRLab);
            htcVive.setExpiryDate(7);
            htcVive.setPrice(new BigDecimal("10.00"));
            htcVive.setAccessories("[\"2x Controller\", \"2x Basisstationen\", \"Linkbox\"]");

            // Foto Products
            Product sonyA7 = new Product();
            sonyA7.setName("Sony Alpha 7 IV");
            sonyA7.setDescription("Spiegellose Vollformatkamera mit 33 Megapixel und 4K Video.");
            sonyA7.setCategory(catPhoto);
            sonyA7.setLocation(locKEIM);
            sonyA7.setExpiryDate(7);
            sonyA7.setPrice(new BigDecimal("15.00"));
            sonyA7.setAccessories("[\"Objektiv 28-70mm\", \"Akku\", \"Ladegeraet\", \"Tragegurt\"]");

            Product canonEOS = new Product();
            canonEOS.setName("Canon EOS R5");
            canonEOS.setDescription("Professionelle Vollformatkamera mit 45 MP und 8K Video.");
            canonEOS.setCategory(catPhoto);
            canonEOS.setLocation(locBib);
            canonEOS.setExpiryDate(7);
            canonEOS.setPrice(new BigDecimal("20.00"));

            // Audio Products
            Product rodeNT1 = new Product();
            rodeNT1.setName("Rode NT1-A Mikrofon");
            rodeNT1.setDescription("Studiomikrofon mit niedrigem Eigenrauschen, ideal fuer Podcasts.");
            rodeNT1.setCategory(catAudio);
            rodeNT1.setLocation(locKEIM);
            rodeNT1.setExpiryDate(14);
            rodeNT1.setPrice(new BigDecimal("3.00"));
            rodeNT1.setAccessories("[\"Spinne\", \"Popschutz\", \"XLR-Kabel\"]");

            // IT Products
            Product macbookPro = new Product();
            macbookPro.setName("MacBook Pro 14 Zoll M3");
            macbookPro.setDescription("Apple MacBook Pro mit M3 Chip, 18GB RAM, 512GB SSD.");
            macbookPro.setCategory(catIT);
            macbookPro.setLocation(locKEIM);
            macbookPro.setExpiryDate(7);
            macbookPro.setPrice(new BigDecimal("25.00"));
            macbookPro.setAccessories("[\"Ladegeraet\", \"USB-C Hub\"]");

            productRepo.save(metaQuest3);
            productRepo.save(htcVive);
            productRepo.save(sonyA7);
            productRepo.save(canonEOS);
            productRepo.save(rodeNT1);
            productRepo.save(macbookPro);

            System.out.println(productRepo.count() + " Products erstellt");

            // ==========================================
            // 5. Items erstellen (physische Exemplare)
            // ==========================================

            // Meta Quest 3 - 3 Stueck (Verleiher: Christian)
            Item quest1 = new Item();
            quest1.setInvNumber("VR-001");
            quest1.setOwner("Christian Haas");
            quest1.setProduct(metaQuest3);
            quest1.setLender(verleiherChristian);

            Item quest2 = new Item();
            quest2.setInvNumber("VR-002");
            quest2.setOwner("Christian Haas");
            quest2.setProduct(metaQuest3);
            quest2.setLender(verleiherChristian);

            Item quest3 = new Item();
            quest3.setInvNumber("VR-003");
            quest3.setOwner("Christian Haas");
            quest3.setProduct(metaQuest3);
            quest3.setLender(verleiherChristian);

            // HTC Vive - 1 Stueck (Verleiher: Christian)
            Item vive1 = new Item();
            vive1.setInvNumber("VR-010");
            vive1.setOwner("Christian Haas");
            vive1.setProduct(htcVive);
            vive1.setLender(verleiherChristian);

            // Sony A7 - 2 Stueck (Verleiher: Christian)
            Item sony1 = new Item();
            sony1.setInvNumber("CAM-001");
            sony1.setOwner("Christian Haas");
            sony1.setProduct(sonyA7);
            sony1.setLender(verleiherChristian);

            Item sony2 = new Item();
            sony2.setInvNumber("CAM-002");
            sony2.setOwner("Christian Haas");
            sony2.setProduct(sonyA7);
            sony2.setLender(verleiherChristian);

            // Canon - 1 Stueck (Verleiher: Andreas)
            Item canon1 = new Item();
            canon1.setInvNumber("CAM-010");
            canon1.setOwner("Andreas Heinrich");
            canon1.setProduct(canonEOS);
            canon1.setLender(verleiherAndreas);

            // Mikrofon - 2 Stueck (Verleiher: Andreas)
            Item rode1 = new Item();
            rode1.setInvNumber("AUD-001");
            rode1.setOwner("KEIM");
            rode1.setProduct(rodeNT1);
            rode1.setLender(verleiherAndreas);

            Item rode2 = new Item();
            rode2.setInvNumber("AUD-002");
            rode2.setOwner("KEIM");
            rode2.setProduct(rodeNT1);
            rode2.setLender(verleiherAndreas);

            // MacBook - 2 Stueck (Verleiher: Andreas)
            Item mac1 = new Item();
            mac1.setInvNumber("IT-001");
            mac1.setOwner("KEIM");
            mac1.setProduct(macbookPro);
            mac1.setLender(verleiherAndreas);

            Item mac2 = new Item();
            mac2.setInvNumber("IT-002");
            mac2.setOwner("KEIM");
            mac2.setProduct(macbookPro);
            mac2.setLender(verleiherAndreas);

            itemRepo.save(quest1);
            itemRepo.save(quest2);
            itemRepo.save(quest3);
            itemRepo.save(vive1);
            itemRepo.save(sony1);
            itemRepo.save(sony2);
            itemRepo.save(canon1);
            itemRepo.save(rode1);
            itemRepo.save(rode2);
            itemRepo.save(mac1);
            itemRepo.save(mac2);

            System.out.println(itemRepo.count() + " Items erstellt");

            // ==========================================
            // Zusammenfassung
            // ==========================================
            System.out.println("\n========================================");
            System.out.println(" TESTDATEN ERFOLGREICH GELADEN!");
            System.out.println("========================================");
            System.out.println(categoryRepo.count() + " Kategorien");
            System.out.println(locationRepo.count() + " Locations");
            System.out.println(userRepo.count() + " User");
            System.out.println(productRepo.count() + " Products");
            System.out.println(itemRepo.count() + " Items");
            System.out.println("========================================");
            System.out.println("API: http://localhost:8080/api/products");
            System.out.println("Swagger: http://localhost:8080/swagger-ui.html");
            System.out.println("H2 Console: http://localhost:8080/h2-console");
            System.out.println("========================================\n");
        };
    }

    @Bean
    @Profile("prod")
    public CommandLineRunner productionStartup() {
        return args -> {
            System.out.println("\n========================================");
            System.out.println(" PRODUKTIONS-MODUS (PostgreSQL)");
            System.out.println("========================================");
            System.out.println(" Keine Testdaten werden geladen!");
            System.out.println("========================================\n");
        };
    }
}