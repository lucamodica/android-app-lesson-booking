package com.example.lessonbooking.view.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.android.volley.Request;
import com.example.lessonbooking.R;
import com.example.lessonbooking.connectivity.RequestManager;
import com.example.lessonbooking.databinding.SelectCourseTeacherBinding;
import com.example.lessonbooking.model.Course;
import com.example.lessonbooking.model.Model;
import com.example.lessonbooking.model.Teacher;
import com.example.lessonbooking.view.activity.LoginActivity;
import com.example.lessonbooking.view.activity.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class BookingFragment extends Fragment implements AdapterView.OnItemSelectedListener{

    public static class BookingViewModel extends ViewModel {

        private MutableLiveData<String> mText;

        public BookingViewModel() {
            mText = new MutableLiveData<>();
            mText.setValue("This is booking fragment");
        }

        public LiveData<String> getText() {
            return mText;
        }
    }


    private BookingViewModel bookingViewModel;
    private SelectCourseTeacherBinding binding;
    private View root;
    private Context ctx;

    Button next_btn;
    Button back_btn;
    TextView teacherTitle;
    TextView waitingCourseText;
    TextView waitingTeacherText;
    Spinner coursesSpinner;
    Spinner teachersSpinner;

    private String account, role;
    private ArrayList<String> listIds;
    private String selected_course, selected_teacher;
    private boolean selecting; //false = course, true = teacher

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        //Context and ViewModel setup
        bookingViewModel =
                new ViewModelProvider(this).get(BookingViewModel.class);
        binding = SelectCourseTeacherBinding.inflate(inflater, container,
                false);
        root = binding.getRoot();
        ctx = root.getContext();

        //Get params from Intent and setting it
        role = ((MainActivity) requireActivity()).getRole();
        if (role.equals("utente") || role.equals("amministratore")){
            account = ((MainActivity) requireActivity()).getAccount();
        }

        //If the user logged is a guest, the login suggest
        //will be showed; otherwise the necessary component
        //will be initialized
        if (role.equals("ospite")){
            showLoginSuggest();
        }
        else {
            initCompnent();
        }

        return root;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (role.equals("utente") || role.equals("amministratore")){
            selecting = false;
            root.findViewById(R.id.spinner_courses).
                    setVisibility(View.GONE);
            fetchData("corso");
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    private void showLoginSuggest(){

        root.findViewById(R.id.select_course_layout).
                setVisibility(View.GONE);
        root.findViewById(R.id.select_teacher_layout).
                setVisibility(View.GONE);
        root.findViewById(R.id.next_back_btns).
                setVisibility(View.GONE);

        root.findViewById(R.id.suggest_login_booking_layout).
                setVisibility(View.VISIBLE);
        root.findViewById(R.id.suggest_login_booking_btn).
                setOnClickListener(v -> logout());
    }
    private void initCompnent(){

        //Next/back buttons
        next_btn = root.findViewById(R.id.button_next);
        back_btn = root.findViewById(R.id.button_back);

        //Spinners
        coursesSpinner = root.findViewById(R.id.spinner_courses);
        teachersSpinner = root.findViewById(R.id.spinner_teachers);

        //TextViews
        teacherTitle = root.findViewById(R.id.select_teacher_title);
        waitingCourseText = root.findViewById(R.id.waiting_course_text);
        waitingTeacherText = root.findViewById(R.id.waiting_teacher_text);
    }
    private void onBackPressed(View v){

    }
    private void onNextPressed(View v){
        if (selecting){
            System.out.println("Bookable slots case (to be done)");
        }
        else {
            Resources r = ctx.getResources();
            int px = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    64,
                    r.getDisplayMetrics()
            );
            LinearLayout courseLayout = root.findViewById(R.id.select_course_layout);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams)
                    courseLayout.getLayoutParams();
            //TODO To be continues

            selecting = true;
            waitingTeacherText.setVisibility(View.VISIBLE);
            teacherTitle.setVisibility(View.VISIBLE);
            coursesSpinner.setEnabled(false);

            fetchData("docente");
        }
    }

    private void logout(){
        String url = getString(R.string.servlet_url) +
                "logout";

        RequestManager.getInstance(ctx).makeRequest(Request.Method.GET,
                url, this::handleLogoutResponse
        );
    }
    private void handleLogoutResponse(JSONObject jsonResult){
        try {
            String status = jsonResult.getString("result");
            switch (status) {
                case "success":

                    String toastText = "";

                    if (!role.equals("ospite")){
                        toastText += "Logout di " + account;
                        System.out.println("User logout: " + account +
                                ", with role '" + role + "'");
                    }
                    else{
                        toastText += "Logout dell'ospite";
                        System.out.println("Guest logout");
                    }

                    //Intent to take the user back to LoginActivity
                    Toast.makeText(ctx, toastText + " avvenuto " +
                            "con successo", Toast.LENGTH_LONG).show();
                    break;

                case "no_user":
                    Toast.makeText(ctx, getString(R.string.no_user_result) + " per " +
                            "effettuare logout", Toast.LENGTH_LONG).show();
                    break;
            }

            requireActivity().startActivity(new Intent(ctx,
                    LoginActivity.class));
            requireActivity().finish();
        }
        catch (JSONException ed) {
            ed.printStackTrace();
        }
    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (selecting){
            selected_teacher = listIds.get(i);
        }
        else {
            selected_course = listIds.get(i);
        }
        System.out.println(listIds.get(i));
    }
    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private void fetchData(String objType){

        //Request url
        String url = getString(R.string.servlet_url) + "selectTable?objType=" +
                objType;

        //Prevent button event
        next_btn.setOnClickListener(null);
        back_btn.setOnClickListener(null);

        //Make request
        RequestManager.getInstance(ctx).makeRequest(
                Request.Method.GET, url,
                success -> handleResponse(success, objType)
        );
    }
    private void createDropdown(JSONArray arr, String objType)
            throws JSONException {

        ArrayList<String> list = new ArrayList<>();
        Model elem;

        listIds = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            elem = (objType.equals("corso")) ? new Course(arr.getJSONObject(i))
                    : new Teacher(arr.getJSONObject(i));
            list.add(elem.toString());
            listIds.add(elem.getModelId());
        }
        System.out.println(list.toString());
        System.out.println(listIds.toString());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(ctx,
                android.R.layout.simple_spinner_dropdown_item, list);
        Spinner spinner = objType.equals("corso") ? coursesSpinner
                        : teachersSpinner;
        spinner.setOnItemSelectedListener(this);
        spinner.setAdapter(adapter);
    }
    private void handleResponse(JSONObject obj, String objType){

        try {
            String result = obj.getString("result");
            switch (result) {
                case "success":
                    if (objType.equals("docente") || objType.equals("corso")){
                        JSONArray arr = obj.getJSONArray("content");
                        createDropdown(arr, objType);
                        setContentLayout(objType, arr.length());
                    }
                    else{
                        System.out.println("Bookable slots case (to be done)");
                    }
                    break;

                case "no_user":
                    Toast.makeText(ctx, R.string.no_user_result,
                            Toast.LENGTH_LONG).show();
                    Intent i = new Intent(ctx, LoginActivity.class);
                    startActivity(i);
                    break;

                case "invalid_object":
                    Toast.makeText(ctx, R.string.invalid_object_result,
                            Toast.LENGTH_LONG).show();
                    break;

                case "not_allowed":
                    Toast.makeText(ctx, R.string.not_allowed_result,
                            Toast.LENGTH_LONG).show();
                    break;

                case "params_null":
                    Toast.makeText(ctx, R.string.params_null_result,
                            Toast.LENGTH_LONG).show();
                    break;

                case "query_failed":
                    Toast.makeText(ctx, R.string.query_failed_result,
                            Toast.LENGTH_LONG).show();
                    break;
            }
        } catch (IllegalStateException | JSONException e) {
            e.printStackTrace();
        }
    }
    @SuppressLint("SetTextI18n")
    private void setContentLayout(String objType, int result_length){

        //Show spinner
        Spinner spinner = objType.equals("corso") ? coursesSpinner
                : teachersSpinner;
        spinner.setVisibility(View.VISIBLE);

        //Buttons handle
        next_btn.setVisibility(View.VISIBLE);
        next_btn.setOnClickListener(this::onNextPressed);
        back_btn.setVisibility((objType.equals("corso")) ? View.GONE
                : View.VISIBLE);
        back_btn.setOnClickListener(this::onBackPressed);

        //Waiting text handle
        TextView waitingText = (objType.equals("corso")) ? waitingCourseText
                : waitingTeacherText;
        waitingText.setVisibility(View.GONE);

        //Case in which none of the teacher teach the couse
        //previously selected
        if (objType.equals("docente") && result_length == 0){
            next_btn.setVisibility(View.GONE);
            waitingText.setVisibility(View.VISIBLE);
            waitingText.setText(
                    "Nessun docente disponibile per la materia: " + selected_course
            );
        }
    }
}