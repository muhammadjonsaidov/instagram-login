# Instagram Business Discovery Application

Bu loyiha Instagram Business API yordamida boshqa foydalanuvchilarning statistikalarini ko'rish va tahlil qilish uchun yaratilgan.

## Xususiyatlar

- **Instagram Business Login**: Facebook OAuth orqali Instagram Business akkauntiga ulanish
- **Business Discovery**: Boshqa Instagram business akkauntlarini qidirish va tahlil qilish
- **Detailed Analytics**: Followers, engagement rate, media insights va boshqa statistikalar
- **Search History**: Qidiruv tarixi va natijalarni saqlash
- **Rate Limiting**: Soatiga 200 ta qidiruv cheklovi
- **Caching**: Tez kirish uchun natijalarni keshlash

## Texnologiyalar

- **Backend**: Spring Boot 3.2.0, Java 17
- **Database**: PostgreSQL
- **Frontend**: Thymeleaf, Bootstrap 5, JavaScript
- **Cache**: Caffeine Cache
- **API**: Instagram Basic Display API, Facebook Graph API

## O'rnatish va Ishga Tushirish

### 1. Talablar

- Java 17+
- PostgreSQL 12+
- Maven 3.6+
- Facebook Developer Account
- Instagram Business Account

### 2. Facebook App Sozlash

1. [Facebook Developers](https://developers.facebook.com/) saytiga kiring
2. Yangi app yarating
3. Instagram Basic Display mahsulotini qo'shing
4. OAuth Redirect URI ni qo'shing: `http://localhost:8080/facebook/callback`
5. App ID va App Secret ni oling

### 3. Database Sozlash

```sql
CREATE DATABASE instagram_business_discovery;
CREATE USER user WITH PASSWORD 'password';
GRANT ALL PRIVILEGES ON DATABASE instagram_business_discovery TO user;
```

### 4. Environment Variables

`.env` faylini tahrirlang:

```env
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/instagram_business_discovery
SPRING_DATASOURCE_USERNAME=user
SPRING_DATASOURCE_PASSWORD=password

# Facebook/Instagram API
FACEBOOK_CLIENT_ID=your_app_id
FACEBOOK_CLIENT_SECRET=your_app_secret
FACEBOOK_REDIRECT_URI=http://localhost:8080/facebook/callback
```

### 5. Loyihani Ishga Tushirish

```bash
# Dependencies o'rnatish
mvn clean install

# Ilovani ishga tushirish
mvn spring-boot:run
```

Ilova `http://localhost:8080` da ishga tushadi.

## Foydalanish

### 1. Instagram Business Account Ulash

1. Asosiy sahifada "Connect Instagram Business Account" tugmasini bosing
2. Facebook/Instagram login qiling
3. Ruxsatlarni tasdiqlang

### 2. Business Discovery

1. Dashboard sahifasida "Start Business Discovery" tugmasini bosing
2. Qidirilayotgan Instagram username ni kiriting (masalan: `nike`, `cocacola`)
3. "Include Recent Media" ni belgilang (ixtiyoriy)
4. "Search" tugmasini bosing

### 3. Natijalarni Ko'rish

Qidiruv natijasida quyidagi ma'lumotlar ko'rsatiladi:

- **Asosiy ma'lumotlar**: Username, ism, bio, profil rasmi
- **Statistikalar**: Followers, following, posts soni
- **Account turi**: Business, Creator yoki Personal
- **Engagement metrics**: O'rtacha engagement rate
- **Recent media**: So'nggi postlar va ularning statistikalari

## API Endpoints

### Authentication
- `GET /` - Asosiy sahifa
- `GET /login` - Instagram login
- `GET /facebook/callback` - OAuth callback

### Dashboard
- `GET /dashboard` - Foydalanuvchi dashboard
- `GET /dashboard/profile` - Profil sahifasi

### Business Discovery
- `GET /business-discovery` - Qidiruv sahifasi
- `POST /business-discovery/search` - Business account qidirish
- `GET /business-discovery/history` - Qidiruv tarixi
- `GET /business-discovery/stats` - Qidiruv statistikalari

## Rate Limiting

- Soatiga 200 ta qidiruv
- Har bir foydalanuvchi uchun alohida limit
- Keshlangan natijalar limitga kirmaydi

## Xatoliklar va Yechimlar

### "User does not manage any Facebook Pages"
- Instagram Business akkauntingiz Facebook Page bilan bog'langanligini tekshiring

### "Business discovery data not found"
- Target username to'g'ri ekanligini tekshiring
- Akkaunt public va business type ekanligini tasdiqlang

### "Rate limit exceeded"
- 1 soat kutib, qayta urinib ko'ring
- Keshlangan natijalardan foydalaning

## Loyiha Strukturasi

```
src/main/java/com/instagram/businessdiscovery/
├── config/          # Configuration classes
├── controller/      # REST controllers
├── domain/          # Entity classes
├── dto/             # Data Transfer Objects
├── repository/      # JPA repositories
├── service/         # Business logic
└── InstagramBusinessDiscoveryApplication.java

src/main/resources/
├── templates/       # Thymeleaf templates
└── application.yml  # Configuration
```

## Kelajakdagi Yaxshilanishlar

- [ ] Real-time notifications
- [ ] Export functionality (PDF, Excel)
- [ ] Advanced analytics dashboard
- [ ] Competitor comparison
- [ ] Scheduled searches
- [ ] API rate limit optimization
- [ ] Mobile responsive improvements

## Muallif

Bu loyiha Instagram Business API va Spring Boot texnologiyalari yordamida yaratilgan.

## Litsenziya

MIT License