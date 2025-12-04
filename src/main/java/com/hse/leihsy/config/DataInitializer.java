package com.hse.leihsy.config;

import com.hse.leihsy.model.entity.*;
import com.hse.leihsy.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;

/**
 * DataInitializer - Lädt Testdaten beim Start
 * NUR AKTIV IM DEV-PROFIL
 *
 * In Produktion (PostgreSQL) werden keine Testdaten geladen
 */
@Configuration
public class DataInitializer {

    /**
     * Testdaten laden - nur im Development Profil
     */
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
            Category catIT = new Category("IT-Geräte");

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
            Product metaQuest3 = new Product("Meta Quest 3",
                    "Die Meta Quest 3 ist ein Mixed-Reality-Headset mit hochauflösendem Display und verbessertem Tracking.");
            metaQuest3.setCategory(catVR);
            metaQuest3.setLocation(locVRLab);
            //metaQuest3.setLender(verleiherChristian);
            metaQuest3.setExpiryDate(14);
            metaQuest3.setPrice(new BigDecimal("5.00"));
            metaQuest3.setAccessories("[\"2x Controller\", \"Ladekabel USB-C\", \"Tragetasche\"]");
            metaQuest3.setImageUrl("/assets/images/meta-quest-3.jpg");

            Product htcVive = new Product("HTC Vive Pro 2",
                    "Professionelles VR-Headset mit 5K-Auflösung und präzisom Tracking.");
            htcVive.setCategory(catVR);
            htcVive.setLocation(locVRLab);
            //htcVive.setLender(verleiherChristian);
            htcVive.setExpiryDate(7);
            htcVive.setPrice(new BigDecimal("10.00"));
            htcVive.setAccessories("[\"2x Controller\", \"2x Basisstationen\", \"Linkbox\"]");

            // Foto Products
            Product sonyA7 = new Product("Sony Alpha 7 IV",
                    "Spiegellose Vollformatkamera mit 33 Megapixel und 4K Video.");
            sonyA7.setCategory(catPhoto);
            sonyA7.setLocation(locKEIM);
            //sonyA7.setLender(verleiherChristian);
            sonyA7.setExpiryDate(7);
            sonyA7.setPrice(new BigDecimal("15.00"));
            sonyA7.setAccessories("[\"Objektiv 28-70mm\", \"Akku\", \"Ladegerät\", \"Tragegurt\"]");

            Product canonEOS = new Product("Canon EOS R5",
                    "Professionelle Vollformatkamera mit 45 MP und 8K Video.");
            canonEOS.setCategory(catPhoto);
            canonEOS.setLocation(locBib);
            //canonEOS.setLender(verleiherAndreas);
            canonEOS.setExpiryDate(7);
            canonEOS.setPrice(new BigDecimal("20.00"));

            // Audio Products
            Product rodeNT1 = new Product("Rode NT1-A Mikrofon",
                    "Studiomikrofon mit niedriegem Eigenrauschen, ideal fuer Podcasts.");
            rodeNT1.setCategory(catAudio);
            rodeNT1.setLocation(locKEIM);
            //rodeNT1.setLender(verleiherAndreas);
            rodeNT1.setExpiryDate(14);
            rodeNT1.setPrice(new BigDecimal("3.00"));
            rodeNT1.setAccessories("[\"Spinne\", \"Popschutz\", \"XLR-Kabel\"]");

            // IT Products
            Product macbookPro = new Product("MacBook Pro 14\" M3",
                    "Apple MacBook Pro mit M3 Chip, 18GB RAM, 512GB SSD.");
            macbookPro.setCategory(catIT);
            macbookPro.setLocation(locKEIM);
            //macbookPro.setLender(verleiherAndreas);
            macbookPro.setExpiryDate(7);
            macbookPro.setPrice(new BigDecimal("25.00"));
            macbookPro.setAccessories("[\"Ladegerät\", \"USB-C Hub\"]");

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

            // Meta Quest 3 - 3 Stück
            Item quest1 = new Item("VR-001", "Christian Haas", metaQuest3);
            Item quest2 = new Item("VR-002", "Christian Haas", metaQuest3);
            Item quest3 = new Item("VR-003", "Christian Haas", metaQuest3);

            // HTC Vive - 1 Stück
            Item vive1 = new Item("VR-010", "Christian Haas", htcVive);

            // Sony A7 - 2 Stück
            Item sony1 = new Item("CAM-001", "Christian Haas", sonyA7);
            Item sony2 = new Item("CAM-002", "Christian Haas", sonyA7);

            // Canon - 1 Stück
            Item canon1 = new Item("CAM-010", "Andreas Heinrich", canonEOS);

            // Mikrofon - 2 Stück
            Item rode1 = new Item("AUD-001", "KEIM", rodeNT1);
            Item rode2 = new Item("AUD-002", "KEIM", rodeNT1);

            // MacBook - 2 Stück
            Item mac1 = new Item("IT-001", "KEIM", macbookPro);
            Item mac2 = new Item("IT-002", "KEIM", macbookPro);

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
            System.out.println("H2 Console: http://localhost:8080/h2-console");
            System.out.println("========================================\n");
        };
    }

    /**
     * Produktion - keine Testdaten laden
     */
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