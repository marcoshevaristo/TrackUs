package com.marcosevaristo.tcc001.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.marcosevaristo.tcc001.App;
import com.marcosevaristo.tcc001.R;
import com.marcosevaristo.tcc001.adapters.ViewPagerAdapter;
import com.marcosevaristo.tcc001.database.QueryBuilder;
import com.marcosevaristo.tcc001.fragments.AbaBuscar;
import com.marcosevaristo.tcc001.fragments.AbaFavoritos;

public class MainActivity extends AppCompatActivity {

    private ViewPagerAdapter viewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupMainActivity();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            for(int i = 0; i < viewPagerAdapter.getCount(); i++) {
                if (i == 0) {
                    ((AbaBuscar) viewPagerAdapter.getItem(i)).atualizaBusca();
                } else if (i == 1) {
                    ((AbaFavoritos) viewPagerAdapter.getItem(i)).atualizaFavoritos();
                }
            }
        }
    }

    private void setupMainActivity() {
        App.setMunicipio(QueryBuilder.getMunicipioAtual());
        if(App.getMunicipio() == null) {
            startActivityForResult(new Intent(App.getAppContext(), SelecionaMunicipio.class), 0);
        } else {
            setupToolbar();
            setContentView(R.layout.activity_main);
            setupTabLayout();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void setupToolbar() {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
    }

    private void setupTabLayout() {
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.addTab(tabLayout.newTab().setText(R.string.abaBuscar));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.abaFavoritos));

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);

        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragment(new AbaBuscar(), getString(R.string.abaBuscar));
        viewPagerAdapter.addFragment(new AbaFavoritos(), getString(R.string.abaFavoritos));
        viewPager.setAdapter(viewPagerAdapter);

        tabLayout.setupWithViewPager(viewPager);
        viewPager.addOnPageChangeListener(getOnPageChangeListener(viewPagerAdapter));
    }

    private ViewPager.OnPageChangeListener getOnPageChangeListener(final ViewPagerAdapter adapter) {
        return new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    ((AbaBuscar) adapter.getItem(position)).atualizaBusca();
                } else if (position == 1) {
                    ((AbaFavoritos) adapter.getItem(position)).atualizaFavoritos();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        };
    }
}
