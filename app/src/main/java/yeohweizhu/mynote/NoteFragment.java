package yeohweizhu.mynote;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class NoteFragment extends Fragment implements NoteService.INoteServiceListChangedEventListener {
    private static final String TAG = "NoteFragment";
    private static final String NOTESERVICETAG="NoteService";

    //All Required Dependency Service
    private NoteService mNoteService;

    // TODO: Rename and change types of parameters
    private String mParam2;

    private OnFragmentInteractionListener mListener; //To notify activity for event

    //Fragment Recycler
    private RecyclerView mPinnedRecyclerView;
    private NoteSummaryAdapter mPinnedSummaryViewAdapter;

    private RecyclerView mUnpinnedSummaryRecyclerView;
    private NoteSummaryAdapter mUnpinnedSummaryViewAdapter;

    private int mStatusBarColor;

    //Factory Method to automatically construct correct argument
    public static NoteFragment newInstance(NoteService paramNoteService) {
        NoteFragment fragment = new NoteFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    //DO NOT CALL THIS METHOD DIRECTLY, USE FACTORY  else you gonna get exception
    public NoteFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            mNoteService = NoteService.getInstance(getContext());
        }
        else{
            throw new NullPointerException("Recommended:NewInstance static method to create Note Fragment Instance");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mStatusBarColor = getActivity().getWindow().getStatusBarColor();
        }

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_note, container, false);

        //Setting Toolbar
        setupToolbar(v);
        //bar.setDrawerIndicatorEnabled(true);

        //Recycler View
        mPinnedRecyclerView = (RecyclerView) v.findViewById(R.id.fragment_note_recycler);
        mUnpinnedSummaryRecyclerView = (RecyclerView) v.findViewById(R.id.fragment_note_unpineed_recycler);
        mPinnedSummaryViewAdapter = new NoteSummaryAdapter(getActivity(),mNoteService.getNoteList(), NoteSummaryAdapter.RenderOption.RENDER_PINNED, getArchiveRenderOption());
        mUnpinnedSummaryViewAdapter = new NoteSummaryAdapter(getActivity(),mNoteService.getNoteList(),NoteSummaryAdapter.RenderOption.RENDER_UNPINNED, getArchiveRenderOption());
        mPinnedSummaryViewAdapter.setSynchronizedAdapter(mUnpinnedSummaryViewAdapter);
        mUnpinnedSummaryViewAdapter.setSynchronizedAdapter(mPinnedSummaryViewAdapter);
        mPinnedRecyclerView.setNestedScrollingEnabled(false);
        mUnpinnedSummaryRecyclerView.setNestedScrollingEnabled(false);

        StaggeredGridLayoutManager mLayoutManager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        //mLayoutManager.setSpanCount(1);
        mPinnedRecyclerView.setLayoutManager(mLayoutManager);
        mPinnedRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mPinnedRecyclerView.setAdapter(mPinnedSummaryViewAdapter);
        mPinnedRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), mPinnedRecyclerView, new ITouchListener() {
            @Override
            public void onTouch(View view, int position) {
                Note targetNote = mPinnedSummaryViewAdapter.getNoteList().get(position);

                onSummaryViewTouch(targetNote);
            }

            @Override
            public void onLongTouch(View view, int position) {
                //TODO On Long Touch allow multiple selection
            }
        })
        );
        ItemTouchHelper.Callback callback =
                new ItemTouchHelperCallBackPinGrid(mPinnedSummaryViewAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(mPinnedRecyclerView);

        mLayoutManager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        mUnpinnedSummaryRecyclerView.setLayoutManager(mLayoutManager);
        mUnpinnedSummaryRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mUnpinnedSummaryRecyclerView.setAdapter(mUnpinnedSummaryViewAdapter);
        mUnpinnedSummaryRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), mUnpinnedSummaryRecyclerView, new ITouchListener() {
                    @Override
                    public void onTouch(View view, int position) {
                        Note targetNote = mUnpinnedSummaryViewAdapter.getNoteList().get(position);

                        onSummaryViewTouch(targetNote);
                    }

                    @Override
                    public void onLongTouch(View view, int position) {
                        //TODO On Long Touch allow multiple selection
                    }
                })
        );
        callback =
                new ItemTouchHelperCallBackPinGrid(mUnpinnedSummaryViewAdapter);
        touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(mUnpinnedSummaryRecyclerView);

        mNoteService.addOnNoteListChangedListener(this);

        //Setup Listeners for Toolbar (bottom)

        if (showBottomToolbar()){
            TextView addnoteTextView = (TextView) v.findViewById(R.id.toolbar_addnote_default);
            addnoteTextView.setOnClickListener(
                    new View.OnClickListener(){
                        @Override
                        public void onClick(View view) {
                            StartFullDetailNoteActivity(null);
                        }
                    }
            );
        }
        else{
            View view = v.findViewById(R.id.fragment_note_toolbar_addnote);
            view.setVisibility(View.GONE);
        }

        return v;
    }

    @Override
    public void onPause(){
        mNoteService.saveNoteOrder(); //Save ordering

        super.onPause();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    //region Action Bar/Toolbar Menu Handling
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        int menuId = getToolbarMenuID();

        inflater.inflate(menuId,menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.toolbar_main_action_search:
//                //Open Search Activity
//
//                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    //region NoteListItemChanged Update Recycler. Service will broadcast list of notes changes, this method will respond to it and then proceed to relay the message to Adapter
    @Override
    public void NoteListItemChangedEvent(Object sender, NoteService.ListItemChangedArgs eventArgs) {
        switch(eventArgs.status){
            case INSERTED:
                mPinnedSummaryViewAdapter.notifyItemInserted(eventArgs.position);
                mUnpinnedSummaryViewAdapter.notifyItemInserted(eventArgs.position);
                break;
            case CHANGED:
                mPinnedSummaryViewAdapter.notifyItemChanged(eventArgs.position);
                mUnpinnedSummaryViewAdapter.notifyItemChanged(eventArgs.position);
                break;
            case DELETED:
                mPinnedSummaryViewAdapter.notifyItemRemoved(eventArgs.position);
                mUnpinnedSummaryViewAdapter.notifyItemRemoved(eventArgs.position);
                break;
            default:
                //Last Resort
                mPinnedSummaryViewAdapter.notifyDataSetChanged();
                mUnpinnedSummaryViewAdapter.notifyDataSetChanged();
                break;
        }
    }
    //endregion

    protected void StartFullDetailNoteActivity(Note targetNote){
        Intent intent = new Intent(getActivity(),FullDetailNoteActivity.class);
        Bundle b = new Bundle();

        if (targetNote!=null)
            b.putParcelable(FullDetailNoteActivity.NOTEOBJ, targetNote);

        intent.putExtras(b);
        startActivity(intent);
    }

    //Override this to display different toolbar
    protected void setupToolbar(View v){
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar_main);
        toolbar.setTitle("My Note");
        toolbar.setTitleTextColor(ContextCompat.getColor(getActivity(),R.color.colorWhite));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int color = ContextCompat.getColor(getActivity(),R.color.colorPrimaryDark);
            //int targetColor = Color.argb(100,Color.red(color),Color.red(color),Color.blue(color));
            //int targetColor = Color.argb(0,0,0,0);
            getActivity().getWindow().setStatusBarColor(color);
        }

        activity.setSupportActionBar(toolbar);
        ActionBar bar = activity.getSupportActionBar();
        bar.setHomeButtonEnabled(true);
        bar.setDisplayHomeAsUpEnabled(true);

        ((IActivityFragmentHost) getActivity()).showHamburgerIcon();
    }

    //Override this to give proper toolbar menu
    protected int getToolbarMenuID(){
        return (R.menu.toolbar_main_items);
    }

    //Override this to respond to Note summary on touch
    protected void onSummaryViewTouch(Note targetNote){
        StartFullDetailNoteActivity(targetNote);
    }

    //Override this to respond to Note summary on long touch
    protected void onSummaryViewLongTouch(Note tagetNote){

    }

    //Override this to not show bottom toolbar
    protected boolean showBottomToolbar(){
        return true;
    }

    //Override this to render what archive, unarchive, all
    protected NoteSummaryAdapter.ArchiveOption getArchiveRenderOption(){
        return NoteSummaryAdapter.ArchiveOption.RENDER_NORMAL;
    }
}
