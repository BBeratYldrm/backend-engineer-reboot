# Day 34 — May 19, 2026

## What I learned

### Algorithm — Two Pointer
- RemoveDuplicates — mantık tam oturdu bu sefer
    - left = son unique pozisyonu, right = kaşif
    - right yeni unique bulunca left genişler ve oraya yazar
    - geri kalan sağ taraf çöp, kimse bakmıyor
    - return left + 1 çünkü index 0'dan başlıyor

- MergeSortedArray — three pointer, sondan başlama
    - pointer1 = nums1'in son gerçek elemanı
    - pointer2 = nums2'nin sonu
    - writePosition = nums1'in son indexi
    - sondan başlama sebebi: baştan başlasak gerçek elemanları ezeriz
    - nums2 bitince nums1'de kalanlar zaten yerinde

## How I feel
Hafif bir akşamdı. Beyin tam çalışmıyordu ama yine de bir şeyler çıktı.
RemoveDuplicates mantığı bugün gerçekten oturdu — "right unique bulunca
sola taşıyor, left pozisyonu işaretliyor" cümlesi her şeyi netleştirdi.
Sıfır gün geçirmemek önemli, tempo korundu.

## Next
- Two Pointer devam — Remove Element, Majority Element
- System Design — SD-06 Search Autocomplete
- Java Interview Crash Course — Modül 1
- AWS basics