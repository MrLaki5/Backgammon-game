# Economic Model — Royal Backgammon

## Revenue Formula

```
Daily Ad Revenue = DAU × Games/DAU × Ads/Game × eCPM / 1000
```

Expanded:
```
Daily Revenue = DAU × Sessions/DAU × Games/Session × Ad_Rate × Fill_Rate × eCPM / 1000
```

Where:
- **DAU** = Daily Active Users
- **Games/DAU** = Average completed games per user per day
- **Ads/Game** = Fraction of games that show an interstitial (currently 1/3)
- **eCPM** = Effective Cost Per Mille (revenue per 1000 impressions)
- **Fill_Rate** = % of ad requests that actually serve an ad

---

## Current Ad Policy

| Parameter | Value | Note |
|-----------|-------|------|
| Interstitial frequency | 1 per 3 games | `AdConfig.INTERSTITIAL_EVERY_N_GAMES = 3` |
| Rewarded (optional) | On demand (hint) | Not counted in base revenue |
| Banner | None | Policy: never on game screen |
| Ad SDK | TapsellPlus 2.2.4 | Iranian ad network |

---

## Assumptions

| Parameter | Estimated Value | Confidence |
|-----------|----------------|------------|
| Games per DAU | 3-5 | Medium (needs data) |
| Sessions per DAU | 1.5-2 | Medium |
| Interstitial per game | 0.33 (1/3) | Known (policy) |
| Fill rate (Tapsell, Iran) | UNKNOWN | Need real data |
| eCPM (Tapsell, Iran, gaming) | UNKNOWN | Need real data |
| Rewarded completion rate | UNKNOWN | Need real data |

> ⚠️ **eCPM and fill rate are UNKNOWN.** Real values can only be determined after launching with live traffic. Iranian ad market eCPMs typically range $0.30-$2.00 depending on ad network, format, and fill.

---

## Scenario Analysis

### Conservative assumptions:
- Games/DAU: 3
- Ads/Game: 0.33
- Fill rate: 70%
- eCPM: $0.50

### Moderate assumptions:
- Games/DAU: 4
- Ads/Game: 0.33
- Fill rate: 80%
- eCPM: $1.00

### Optimistic assumptions:
- Games/DAU: 5
- Ads/Game: 0.33
- Fill rate: 90%
- eCPM: $1.50

---

## Revenue Projections

### 100 DAU

| Scenario | Daily Impressions | Daily Revenue |
|----------|------------------|---------------|
| Conservative | 100 × 3 × 0.33 × 0.7 = 69 | 69 × $0.50 / 1000 = **$0.03** |
| Moderate | 100 × 4 × 0.33 × 0.8 = 106 | 106 × $1.00 / 1000 = **$0.11** |
| Optimistic | 100 × 5 × 0.33 × 0.9 = 149 | 149 × $1.50 / 1000 = **$0.22** |

### 500 DAU

| Scenario | Daily Impressions | Daily Revenue |
|----------|------------------|---------------|
| Conservative | 347 | **$0.17** |
| Moderate | 528 | **$0.53** |
| Optimistic | 743 | **$1.11** |

### 1,000 DAU

| Scenario | Daily Impressions | Daily Revenue | Monthly |
|----------|------------------|---------------|---------|
| Conservative | 693 | **$0.35** | $10.5 |
| Moderate | 1,056 | **$1.06** | $31.7 |
| Optimistic | 1,485 | **$2.23** | $66.8 |

### 5,000 DAU

| Scenario | Daily Impressions | Daily Revenue | Monthly |
|----------|------------------|---------------|---------|
| Conservative | 3,465 | **$1.73** | $52 |
| Moderate | 5,280 | **$5.28** | $158 |
| Optimistic | 7,425 | **$11.14** | $334 |

### 10,000 DAU

| Scenario | Daily Impressions | Daily Revenue | Monthly |
|----------|------------------|---------------|---------|
| Conservative | 6,930 | **$3.47** | $104 |
| Moderate | 10,560 | **$10.56** | $317 |
| Optimistic | 14,850 | **$22.28** | $668 |

---

## Revenue Levers

| Lever | Impact | Risk |
|-------|--------|------|
| Increase Games/DAU (retention) | High | Low — better game = more games |
| Increase DAU (acquisition) | High | Cost dependent (CPI) |
| Add Rewarded ads | Medium | Low — user-initiated |
| Increase ad frequency | Medium | HIGH — hurts retention |
| Increase eCPM (better ad network) | Medium | Low — just configuration |
| Add IAP (remove ads, themes) | Medium | Low — value exchange |

---

## Unit Economics for Paid Acquisition

```
LTV = (Lifetime Days × Games/Day × Ads/Game × Fill × eCPM / 1000) + IAP Revenue
```

Example (moderate scenario, D30 user):
```
LTV = 30 × 4 × 0.33 × 0.8 × $1.00 / 1000 = $0.032
```

This is VERY LOW. Meaning:
- Paid acquisition is NOT viable at current monetization levels
- Must achieve either: much higher eCPM, add IAP revenue, or rely on organic growth
- CPI must be < $0.03 to be profitable (unrealistic)

**Conclusion:** Focus on organic growth until:
1. Real eCPM data is available
2. IAP revenue is added (remove ads, theme packs)
3. DAU reaches 5,000+ to justify any paid spend

---

## Next Steps

1. **Launch** → get real fill rate and eCPM data from Tapsell
2. **Measure** → Games/DAU, Sessions/DAU, D1/D7 retention
3. **Optimize** → increase Games/DAU through retention features (rematch, streak)
4. **Add IAP** → remove ads ($2-3), theme packs ($1 each)
5. **Re-evaluate** → after 1 month of data, reassess paid acquisition
