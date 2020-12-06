package com.runtimeoverflow.SchulNetzClient.Activities;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.runtimeoverflow.SchulNetzClient.AsyncAction;
import com.runtimeoverflow.SchulNetzClient.Data.Change;
import com.runtimeoverflow.SchulNetzClient.Data.Student;
import com.runtimeoverflow.SchulNetzClient.Data.Teacher;
import com.runtimeoverflow.SchulNetzClient.Data.User;
import com.runtimeoverflow.SchulNetzClient.Parser;
import com.runtimeoverflow.SchulNetzClient.R;
import com.runtimeoverflow.SchulNetzClient.Utilities;
import com.runtimeoverflow.SchulNetzClient.Variables;

import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.Arrays;

public class PeopleFragment extends Fragment {
	Button studentsButton;
	Button teachersButton;
	
	RecyclerView peopleList;
	PeopleAdapter adapter;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		return inflater.inflate(R.layout.people_fragment, container, false);
	}
	
	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		studentsButton = getView().findViewById(R.id.studentsButton);
		teachersButton = getView().findViewById(R.id.teachersButton);
		
		peopleList = getView().findViewById(R.id.peopleList);
		peopleList.setLayoutManager(new LinearLayoutManager(getContext()));
		peopleList.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
		
		adapter = new PeopleAdapter(new ArrayList<String>());
		peopleList.setAdapter(adapter);
		
		studentsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				showStudents();
			}
		});
		
		teachersButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				showTeachers();
			}
		});
		
		adapter.setOnClickListener(new PeopleAdapter.CellListener() {
			@Override
			public void onCellPressed(int index) {
				if(!studentsButton.isEnabled()){
					Student currentStudent = Variables.get().user.students.get(index);
					Variables.get().activityParameter = currentStudent;
					
					startActivity(new Intent(Variables.get().currentContext, StudentActivity.class));
				} else{
					Teacher currentTeacher = Variables.get().user.teachers.get(index);
					Variables.get().activityParameter = currentTeacher;
					
					startActivity(new Intent(Variables.get().currentContext, TeacherActivity.class));
				}
			}
		});
		
		showStudents();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		Utilities.runAsynchronous(new AsyncAction() {
			@Override
			public void runAsync() {
				if(Utilities.hasWifi()){
					Object result = Variables.get().account.loadPage("22352");
					
					if(result != null && result.getClass() == Document.class){
						Parser.parseTeachers((Document) result, Variables.get().user);
					}
					
					result = Variables.get().account.loadPage("22348");
					
					if(result != null && result.getClass() == Document.class){
						User copy = Variables.get().user.copy();
						
						Parser.parseStudents((Document) result, Variables.get().user);
						Variables.get().user.processConnections();
						
						Change.publishNotifications(Change.getChanges(copy, Variables.get().user));
						Variables.get().user.save();
					}
				}
			}
			
			@Override
			public void runSyncWhenDone() {
				reloadList();
			}
		});
	}
	
	private void showStudents(){
		studentsButton.setEnabled(false);
		teachersButton.setEnabled(true);
		
		reloadList();
	}
	
	private void showTeachers(){
		studentsButton.setEnabled(true);
		teachersButton.setEnabled(false);
		
		reloadList();
	}
	
	private void reloadList(){
		if(getView() == null) return;
		
		if(!studentsButton.isEnabled()){
			ArrayList<String> names = new ArrayList<>();
			
			for(Student s : Variables.get().user.students){
				names.add(s.firstName + " " + s.lastName);
			}
			
			adapter.setNames(names);
		} else{
			ArrayList<String> names = new ArrayList<>();
			
			for(Teacher t : Variables.get().user.teachers){
				names.add(t.firstName + " " + t.lastName);
			}
			
			adapter.setNames(names);
		}
	}
}

class PeopleAdapter extends RecyclerView.Adapter<PeopleAdapter.PeopleViewHolder>{
	private ArrayList<String> names = new ArrayList<>();
	private CellListener listener = null;
	
	public static class PeopleViewHolder extends RecyclerView.ViewHolder{
		public TextView nameLabel;
		public int index = 0;
		public PeopleViewHolder(@NonNull View itemView) {
			super(itemView);
			nameLabel = itemView.findViewById(R.id.cellLabel);
		}
	}
	
	public static interface CellListener{
		public void onCellPressed(int index);
	}
	
	public PeopleAdapter(ArrayList<String> names){
		this.names = names;
	}
	
	@NonNull
	@Override
	public PeopleAdapter.PeopleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		final ConstraintLayout layout = (ConstraintLayout)LayoutInflater.from(Variables.get().currentContext).inflate(R.layout.cell, parent, false);
		
		return new PeopleViewHolder(layout);
	}
	
	@Override
	public void onBindViewHolder(@NonNull final PeopleAdapter.PeopleViewHolder holder, final int position) {
		holder.nameLabel.setText(names.get(position));
		
		holder.itemView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) holder.itemView.setBackgroundColor(Color.argb(127, 127, 127, 127));
				else if(motionEvent.getAction() == MotionEvent.ACTION_UP || motionEvent.getAction() == MotionEvent.ACTION_CANCEL) holder.itemView.setBackgroundColor(Color.alpha(0));
				
				if(motionEvent.getAction() == MotionEvent.ACTION_UP && listener != null) listener.onCellPressed(position);
				
				return true;
			}
		});
	}
	
	@Override
	public int getItemCount() {
		return names.size();
	}
	
	public void setNames(ArrayList<String> names) {
		this.names = names;
		notifyDataSetChanged();
	}
	
	public void setOnClickListener(CellListener listener) {
		this.listener = listener;
	}
}