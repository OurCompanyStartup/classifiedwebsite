package com.example.dinner_tonight;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.ProfilePictureView;

public class AuthedFB extends Fragment {
	
	private static final int REAUTH_ACTIVITY_CODE = 0;
	private ProfilePictureView profilePictureView;
	private TextView userNameView;
	private UiLifecycleHelper uiHelper;
	private ListView listView;
	private List<BaseListElement> listElements;
	private Session.StatusCallback callback = new Session.StatusCallback() {
	
		
		@Override
	    public void call(final Session session, final SessionState state, final Exception exception) {
	        onSessionStateChange(session, state, exception);
	    }
	};
	
	private void makeMeRequest(final Session session) {
	    // Make an API call to get user data and define a 
	    // new callback to handle the response.
	    Request request = Request.newMeRequest(session, 
	            new Request.GraphUserCallback() {
	        @Override
	        public void onCompleted(GraphUser user, Response response) {
	            // If the response is successful
	            if (session == Session.getActiveSession()) {
	                if (user != null) {
	                    // Set the id for the ProfilePictureView
	                    // view that in turn displays the profile picture.
	                    profilePictureView.setProfileId(user.getId());
	                    // Set the Textview's text to the user's name.
	                  //  userNameView.setText(user.getName());
	                }
	            }
	            if (response.getError() != null) {
	                // Handle errors, will do so later.
	            }
	        }
	    });
	    request.executeAsync();
	} 
	
	private void onSessionStateChange(final Session session, SessionState state, Exception exception) {
	    if (session != null && session.isOpened()) {
	        // Get the user's data.
	        makeMeRequest(session);
	    }
	}
	
	private class ActionListAdapter extends ArrayAdapter<BaseListElement> {
	    private List<BaseListElement> listElements;

	    public ActionListAdapter(Context context, int resourceId, 
	                             List<BaseListElement> listElements) {
	        super(context, resourceId, listElements);
	        this.listElements = listElements;
	        // Set up as an observer for list item changes to
	        // refresh the view.
	        for (int i = 0; i < listElements.size(); i++) {
	            listElements.get(i).setAdapter(this);
	        }
	    }

	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
	        View view = convertView;
	        if (view == null) {
	            LayoutInflater inflater =
	                    (LayoutInflater) getActivity()
	                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            view = inflater.inflate(R.layout.listitem, null);
	        }

	        BaseListElement listElement = listElements.get(position);
	        if (listElement != null) {
	            view.setOnClickListener(listElement.getOnClickListener());
	            ImageView icon = (ImageView) view.findViewById(R.id.icon);
	            TextView text1 = (TextView) view.findViewById(R.id.text1);
	            TextView text2 = (TextView) view.findViewById(R.id.text2);
	            if (icon != null) {
	                icon.setImageDrawable(listElement.getIcon());
	            }
	            if (text1 != null) {
	                text1.setText(listElement.getText1());
	            }
	            if (text2 != null) {
	                text2.setText(listElement.getText2());
	            }
	        }
	        return view;
	    }

	}
	
	private class PeopleListElement extends BaseListElement {
		
	    public PeopleListElement(int requestCode) {
	        super(
	        		getActivity().getResources().getDrawable(R.drawable.action_people),
	              getActivity().getResources().getString(R.string.action_people),
	              getActivity().getResources().getString(R.string.action_people_default),
	              requestCode);
	    }
	    
	    @Override
	    protected View.OnClickListener getOnClickListener() {
	    	 
	        return new View.OnClickListener() {
	            @Override
	            public void onClick(View view) {
	                // Do nothing for now<del>// Do nothing for now</del>
	    	        startPickerActivity(PickerActivity.FRIEND_PICKER, getRequestCode());
	    	        //getRequestCode()
	            }
	        };
	    }
	}
	
	private void startPickerActivity(Uri data, int requestCode) {
	     Intent intent = new Intent();
	     intent.setData(data);
	     intent.setClass(getActivity(), PickerActivity.class);
	     startActivityForResult(intent, requestCode);
	 }
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    uiHelper = new UiLifecycleHelper(getActivity(), callback);
	    uiHelper.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, 
	        ViewGroup container, Bundle savedInstanceState) {
	    super.onCreateView(inflater, container, savedInstanceState);
	    View view = inflater.inflate(R.layout.authedfb, container, false);
	    listView = (ListView) view.findViewById(R.id.selection_list);
	    try {
   
	 // Find the user's profile picture custom view
	    profilePictureView = (ProfilePictureView) view.findViewById(R.id.profile_pic);
	    profilePictureView.setCropped(true);

	    // Find the user's name view
	    //userNameView = (TextView) view.findViewById(R.id.selection_user_name);
	    // Set up the list view items, based on a list of
	     // BaseListElement items
	     listElements = new ArrayList<BaseListElement>();
	     // Add an item for the friend picker
	     listElements.add(new PeopleListElement(0));
	    // listElements.add(new PeopleListElement(1));
	     // Set the list view adapter
	     listView.setAdapter(new ActionListAdapter(getActivity(), R.id.selection_list, listElements));
	    
	    
	 // Check for an open session
	    Session session = Session.getActiveSession();
	    if (session != null && session.isOpened()) {
	        // Get the user's data
	        makeMeRequest(session);
	    	}	    	    
	    }catch(Exception e) {	    	
	    }
	    return view;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    if (requestCode == REAUTH_ACTIVITY_CODE) {
	        uiHelper.onActivityResult(requestCode, resultCode, data);
	    }else if (resultCode == Activity.RESULT_OK) {
	        // Do nothing for now
	    }
	}
	
	@Override
	public void onResume() {
	    super.onResume();
	    uiHelper.onResume();
	}

	@Override
	public void onSaveInstanceState(Bundle bundle) {
	    super.onSaveInstanceState(bundle);
	    uiHelper.onSaveInstanceState(bundle);
	}

	@Override
	public void onPause() {
	    super.onPause();
	    uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
	    super.onDestroy();
	    uiHelper.onDestroy();
	}
	
}
