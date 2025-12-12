1. Apa yang anda ketahui tentang Rest API?
- Standar komunikasi antar aplikasi menggunakan protokol HTTP (biasanya format JSON). Sifatnya stateless dan menggunakan metode utama: GET (baca), POST (tulis), PUT (edit), dan DELETE (hapus).

2. Apa yang anda ketahui tentang Server side and Client side processing? 
- Server-side: Proses terjadi di backend/server. Mengurus logika bisnis, keamanan, dan database.
- Client-side: Proses terjadi di browser user. Mengurus tampilan (UI) dan interaksi agar aplikasi responsif.

3. Apa yang anda ketahui tentang Monolith dan Microservices, berikan contohnya?
- Monolith: Satu aplikasi besar di mana semua fitur tergabung jadi satu file deploy. Contoh: App Toko Online yang login & transaksi-nya jadi satu code.
- Microservices: Aplikasi dipecah menjadi layanan kecil yang berdiri sendiri-sendiri. Contoh: Service Payment terpisah servernya dengan Service Produk.

4. Apa yang anda ketahui tentang Design pattern inversion of Control serta Dependency Injection?
-IoC: Prinsip di mana alur object diatur oleh framework (Spring), bukan developer.
- DI: Teknik penerapannya; kita tidak perlu tulis new Class() manual, framework otomatis menyuntikkan (inject) object tersebut saat diperlukan.

5. Apa yang anda ketahui tentang Java programming dan Spring framework khususnya spring-boot?
- Java: Bahasa pemrograman OOP yang matang dan platform-independent.

- Spring Boot: Framework Java instan dengan fitur Auto-Configuration dan Embedded Server, bikin kita bisa bangun REST API siap pakai dalam hitungan menit tanpa config rumit.