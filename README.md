# ALEX tunnel App

برنامه‌ی اندروید ALEX tunnel بر پایه‌ی v2rayNG (مجوز GPLv3، پروژه‌ی اصلی: https://github.com/2dust/v2rayNG).
این ریپو فقط برندینگ (اسم، لوگو، تم بنفش) رو روی v2rayNG اعمال و بیلد می‌کنه.

## ساخت APK
Actions ← Build ALEX tunnel APK ← Run workflow ← بعد از تموم شدن، فایل APK رو از Artifacts دانلود کن.

## استفاده
نصب اپ ← Subscription group ← افزودن لینک ساب ← Update ← پینگ و انتخاب سرور ← اتصال.

کانال: https://t.me/alexsupportvpn | پشتیبانی: https://t.me/alexsupportsell

## ظاهر برنامه (UI)
این ریپو روی v2rayNG نسخه‌ی Compose (۲٫۳٫۱۰) یک رابط کاربری جدید می‌ذاره:
- `branding/overlay/...` فایل‌های کاتلین/ریسورس جایگزین یا اضافه‌شده (صفحه‌ی Home، نوار پایین، تم بنفش، آیکون‌ها، رشته‌ها)
- `branding/apply.py` برندینگ + کپی overlay رو روی آخرین چک‌اوت v2rayNG اعمال می‌کنه
- ورودی `upstream_ref` در Actions رو برای ثبات روی یه commit SHA بذار؛ overlay با 2.3.10 نوشته شده.
