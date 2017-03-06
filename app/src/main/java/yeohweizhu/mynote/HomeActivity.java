package yeohweizhu.mynote;

import android.content.res.Configuration;
import android.net.Uri;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity implements NoteFragment.OnFragmentInteractionListener, IActivityFragmentHost{
    //TODO SaveInstance, save all this cache
    //All Service Cache
    //Give application context, to prevent memory leak when this activity is destroyed.
    NoteService mNoteService;

    //Navigation Menu and toolbar
    NavigationView mNavigationView;
    DrawerLayout mDrawerLayout;
    ActionBarDrawerToggle mDrawerToggle;

    //Fragment Manager
    FragmentManager mFragmentManager;
    Fragment mAllNoteFragment;
    Fragment mArchiveFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //Init Service
        mNoteService = NoteService.getInstance(getApplicationContext());

        //Init Fragment related operation
        mAllNoteFragment = NoteFragment.newInstance(mNoteService);
        mArchiveFragment = ArchiveFragment.newInstance(mNoteService);
        mFragmentManager = getSupportFragmentManager();

        FragmentTransaction initTransaction = mFragmentManager.beginTransaction();
        initTransaction.add(R.id.frame_home,mAllNoteFragment);
        initTransaction.commit();

        //Init Navigation menu
        initNavigationMenu();

        /* Setting Toolbar is each fragment responsibility
        // Find the toolbar view inside the activity layout
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);
        */
    }

    private Fragment switchMenuFragment;
    private void initNavigationMenu(){
        mDrawerLayout = (DrawerLayout) findViewById(R.id.activity_home_drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                mDrawerLayout.closeDrawers();
                menuItem.setChecked(true);
                switch (menuItem.getItemId()) {
                    case R.id.nav_home:
                        switchMenuFragment=mAllNoteFragment;
                        break;
                    case R.id.nav_archive:
                        switchMenuFragment=mArchiveFragment;
                        break;
                }
                return true;
            }

            
        });

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                             /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (switchMenuFragment!=null) {
                    fragmentNavigateTo(switchMenuFragment);
                    switchMenuFragment=null;
                }
                invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
        };

        // Defer code dependent on restoration of previous instance state.
        // NB: required for the drawer indicator to show up!
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.addDrawerListener(mDrawerToggle);

        mNavigationView.setCheckedItem(R.id.nav_home);
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    private void fragmentNavigateTo(Fragment targetFragment) {
        FragmentManager fragmentManager = mFragmentManager;
        fragmentManager.beginTransaction()
                .replace(R.id.frame_home, targetFragment)
                .commit();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
    }

    @Override
    public void showHamburgerIcon() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerToggle.syncState();
    }
    @Override
    public void showBackIcon() {
        mDrawerToggle.setDrawerIndicatorEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        if (mDrawerToggle!=null)
            mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mDrawerToggle!=null)
            mDrawerToggle.onConfigurationChanged(newConfig);
    }

    //Action bar menu item selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle;
        // if it returns true, then it has handled the app icon touch event
        if (mDrawerToggle!=null && mDrawerToggle.onOptionsItemSelected(item)) {
            return true; //Consume the event
        }

        //Allow fragment to handle individual button presses
        //Means do nothing here (return false)
        //OR return super.onOptionsItemSelected(item) -> Default is false;
        return false;
    }
}
