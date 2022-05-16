package de.jopa.coronainfo;

import androidx.fragment.app.Fragment;

import android.Manifest;
import android.os.Bundle;
import com.github.appintro.AppIntro;
import com.github.appintro.AppIntroFragment;
import com.github.appintro.model.SliderPage;

public class Intro extends AppIntro {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SliderPage sliderPage1 = new SliderPage();
        sliderPage1.setTitle(getString(R.string.introTitle));
        sliderPage1.setDescription(getString(R.string.introText));
        sliderPage1.setBackgroundColorRes(R.color.medium_blue);
        sliderPage1.setImageDrawable(R.drawable.ic_launcher_foreground);

        SliderPage sliderPage2 = new SliderPage();
        sliderPage2.setTitle(getString(R.string.introTitle));
        sliderPage2.setDescription(getString(R.string.introText2));
        sliderPage2.setBackgroundColorRes(R.color.medium_blue);
        sliderPage2.setImageDrawable(R.drawable.ic_launcher_foreground);

        addSlide(AppIntroFragment.createInstance(sliderPage1));
        addSlide(AppIntroFragment.createInstance(sliderPage2));

        askForPermissions(new String[] {Manifest.permission.CAMERA}, 2, true);

        showStatusBar(true);
        setSystemBackButtonLocked(true);
        setSkipButtonEnabled(true);
    }

    @Override
    protected void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        finish();
    }

    @Override
    protected void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        finish();
    }
}