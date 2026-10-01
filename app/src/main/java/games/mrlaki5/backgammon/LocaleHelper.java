package games.mrlaki5.backgammon;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.LocaleList;

import java.util.Locale;

public final class LocaleHelper {
    private LocaleHelper() {}

    public static Context applySelectedLocale(Context context) {
        String languageCode=GamePreferences.getLanguage(context);
        Locale locale=new Locale(languageCode);
        Locale.setDefault(locale);

        Configuration configuration=new Configuration(context.getResources().getConfiguration());
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N){
            configuration.setLocales(new LocaleList(locale));
        }
        else{
            configuration.locale=locale;
        }
        configuration.setLayoutDirection(locale);
        return context.createConfigurationContext(configuration);
    }
}
