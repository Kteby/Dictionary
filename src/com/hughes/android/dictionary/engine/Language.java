package com.hughes.android.dictionary.engine;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import com.ibm.icu.text.Collator;

public class Language {

  static final Map<String, Language> symbolToLangauge = new LinkedHashMap<String, Language>();

  final String symbol;
  final Locale locale;

  Collator sortCollator;
  final Comparator<String> sortComparator;

  private Collator findCollator;
  final Comparator<String> findComparator;

  public Language(final Locale locale) {
    this.symbol = locale.getLanguage();
    this.locale = locale;

    this.sortComparator = new Comparator<String>() {
      public int compare(final String s1, final String s2) {
        return getSortCollator().compare(textNorm(s1, false), textNorm(s2, false));
      }
    };

    this.findComparator = new Comparator<String>() {
      public int compare(final String s1, final String s2) {
        return getFindCollator().compare(textNorm(s1, false), textNorm(s2, false));
      }
    };
    
    symbolToLangauge.put(symbol.toLowerCase(), this);
  }

  public String textNorm(final String s, final boolean toLower) {
    return toLower ? s.toLowerCase() : s;
  }

  @Override
  public String toString() {
    return locale.toString();
  }
  
  public String getSymbol() {
    return symbol;
  }
  
  public synchronized Collator getFindCollator() {
    if (findCollator == null) {
      findCollator = Collator.getInstance(locale);
      findCollator.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
      findCollator.setStrength(Collator.SECONDARY);
    }
    return findCollator;
  }

  public synchronized Collator getSortCollator() {
    if (sortCollator == null) {
      sortCollator = Collator.getInstance(locale);
      sortCollator.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
      sortCollator.setStrength(Collator.IDENTICAL);
    }
    return sortCollator;
  }

  // ----------------------------------------------------------------

  public static final Language en = new Language(Locale.ENGLISH);
  public static final Language fr = new Language(Locale.FRENCH);
  public static final Language it = new Language(Locale.ITALIAN);

  public static final Language de = new Language(Locale.GERMAN) {
    @Override
    public String textNorm(String token, final boolean toLower) {
      if (toLower) {
        token = token.toLowerCase();
      }
      boolean sub = false;
      // This is meant to be fast: occurrences of ae, oe, ue are probably rare.
      for (int ePos = token.indexOf('e', 1); ePos != -1; ePos = token.indexOf(
          'e', ePos + 1)) {
        final char pre = Character.toLowerCase(token.charAt(ePos - 1));
        if (pre == 'a' || pre == 'o' || pre == 'u') {
          sub = true;
          break;
        }
      }
      if (!sub) {
        return token;
      }
      
      token = token.replaceAll("ae", "ä");
      token = token.replaceAll("oe", "ö");
      token = token.replaceAll("ue", "ü");

      token = token.replaceAll("Ae", "Ä");
      token = token.replaceAll("Oe", "Ö");
      token = token.replaceAll("Ue", "Ü");

      token = token.replaceAll("AE", "Ä");
      token = token.replaceAll("OE", "Ö");
      token = token.replaceAll("UE", "Ü");
      
      return token;   
    }
  };
  
  static {
    for (final String lang : Locale.getISOLanguages()) {
      if (lookup(lang) == null) {
        new Language(new Locale(lang));
      }
    }
  }

  // ----------------------------------------------------------------

  public static Language lookup(final String symbol) {
    return symbolToLangauge.get(symbol.toLowerCase());
  }

}