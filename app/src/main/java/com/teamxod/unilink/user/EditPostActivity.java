package com.teamxod.unilink.user;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.teamxod.unilink.R;
import com.teamxod.unilink.house.House;
import com.teamxod.unilink.roommate.Room;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickResult;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.UUID;

import info.hoang8f.android.segmented.SegmentedGroup;

/* TODO @ Etsu:
    (Try to test it if u can. Not sure if firebase part works. My emulator died) -> jump to line 427 later
   * Gallery and radio buttons
*/
public class EditPostActivity extends AppCompatActivity implements IPickResult, DatePickerDialog.OnDateSetListener {

    /* Copied from another file
    private final int PICK_IMAGE_REQUEST = 71;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    */

    // Database field
    private DatabaseReference mDatabase;

    // Java fields (Used to create House obj and push to firebase)
    private FirebaseAuth mAuth;
    private StorageReference mStorageRef;
    private DatabaseReference post;
    private House old_post;
    private String _posterId;
    private String _postId;
    private String _houseType;
    private String _bedroom_number;
    private String _bathroom_number;
    private String _title;
    private String _location;
    private String _description;
    private String _startDate;
    private String _leaseLength;
    private String _tv;
    private String _ac;
    private String _bus;
    private String _parking;
    private String _videoGame;

    //  ------------- UI fields ---------------- //
    private String _gym;
    private String _laundry;
    private String _pet;
    private GridLayout photoGrid;
    private ArrayList<LinearLayout> photoBoxList;
    private PickImageDialog dialog;
    private ArrayList<Uri> pictureList;
    private LinearLayout roomContainer;
    private ArrayList<LinearLayout> roomBoxList;
    private ArrayList<Room> roomList;
    // edit text
    private EditText title;
    private EditText street;
    private EditText city;
    private EditText start_date;
    private EditText description;

    // Facilities
    private CheckedTextView ac;
    private CheckedTextView allow_pet;
    private CheckedTextView parking;
    private CheckedTextView tv;
    private CheckedTextView video_game;
    private CheckedTextView gym;
    private CheckedTextView laundry;
    private CheckedTextView bus;

    // for validation
    private TextWatcher tw;
    private boolean filledIn;


    // --------------- UI fields above --------------- //

    // helper method for ChekedTextView in XML
    private static void setCheckedTextView(final CheckedTextView ctv) {
        ctv.setCheckMarkDrawable(R.drawable.unchecked);
        ctv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!ctv.isChecked()) {
                    ctv.setChecked(true);
                    ctv.setCheckMarkDrawable(R.drawable.checked);
                } else {
                    ctv.setChecked(false);
                    ctv.setCheckMarkDrawable(R.drawable.unchecked);
                }
            }
        });
    }

    // helper method for implemnet check state
    private static void setCheckedTextView_fromResult(final CheckedTextView ctv, final String result) {
        ctv.setCheckMarkDrawable(R.drawable.unchecked);
        if (result.equals("1")) {
            ctv.setChecked(true);
            ctv.setCheckMarkDrawable(R.drawable.checked);
        } else {
            ctv.setChecked(false);
            ctv.setCheckMarkDrawable(R.drawable.unchecked);
        }
    }

    // Get content of check text view
    private static String isChecked(final CheckedTextView ctv) {
        if (ctv.isChecked())
            return "1";
        else
            return "0";
    }

    // On create
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);

        // variables
        filledIn = false;
        _houseType = "";
        _bedroom_number = "";
        _bathroom_number = "";
        _leaseLength = "";

        // Data base part
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        // get UI in gridlayout
        ImageView addpic = findViewById(R.id.addpic_btn);
        photoGrid = findViewById(R.id.gridlayout);
        photoBoxList = new ArrayList<>();
        pictureList = new ArrayList<>();
        addpic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // pickImage
                dialog = PickImageDialog.build(new PickSetup()).show(EditPostActivity.this);
            }
        });

        // get UI in gridlayout
        Button addroom = findViewById(R.id.addroom_btn);
        roomContainer = findViewById(R.id.linearlayout_room);
        roomBoxList = new ArrayList<>();
        roomList = new ArrayList<>();
        addroom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // pickImage
                Room r = new Room("", -1);
                hideKeyBoard(v);
                addRoom(r);
            }
        });

        title = findViewById(R.id.title);
        street = findViewById(R.id.street);
        city = findViewById(R.id.city);
        RadioButton apartment = findViewById(R.id.apartment);
        RadioButton house = findViewById(R.id.house);
        RadioButton town_house = findViewById(R.id.town_house);
        RadioButton bed_zero = findViewById(R.id.bed_zero);
        RadioButton bed_one = findViewById(R.id.bed_one);
        RadioButton bed_two = findViewById(R.id.bed_two);
        RadioButton bed_three = findViewById(R.id.bed_three);
        RadioButton bed_four_plus = findViewById(R.id.bed_four_plus);
        RadioButton bath_zero = findViewById(R.id.bath_zero);
        RadioButton bath_one = findViewById(R.id.bath_one);
        RadioButton bath_two = findViewById(R.id.bath_two);
        RadioButton bath_three = findViewById(R.id.bath_three);
        RadioButton bath_four_plus = findViewById(R.id.bath_four_plus);
        RadioButton annual = findViewById(R.id.annual);
        RadioButton quarterly = findViewById(R.id.quarterly);
        RadioButton short_term = findViewById(R.id.short_term);
        start_date = findViewById(R.id.start_date);
        ac = findViewById(R.id.ac);
        allow_pet = findViewById(R.id.allow_pet);
        parking = findViewById(R.id.parking);
        tv = findViewById(R.id.tv);
        video_game = findViewById(R.id.video_game);
        gym = findViewById(R.id.gym);
        laundry = findViewById(R.id.laundry);
        bus = findViewById(R.id.bus);
        description = findViewById(R.id.description);

        Button submit = findViewById(R.id.submit);

        // for validation
        tw = new MyTextWatcher();
        title.addTextChangedListener(tw);
        street.addTextChangedListener(tw);
        city.addTextChangedListener(tw);
        description.addTextChangedListener(tw);

        // set check text view, using helper method
        setCheckedTextView(ac);
        setCheckedTextView(allow_pet);
        setCheckedTextView(parking);
        setCheckedTextView(tv);
        setCheckedTextView(video_game);
        setCheckedTextView(gym);
        setCheckedTextView(laundry);
        setCheckedTextView(bus);

        //  -------------- set listeners ---(pass values in xml to java)---------- //
        // date picker
        start_date.setInputType(InputType.TYPE_NULL);
        start_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerFragment datePicker = new DatePickerFragment();
                datePicker.show(getSupportFragmentManager(), "date");
                hideKeyBoard(v);
            }
        });
        // House type
        apartment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _houseType = "Apartment";
                hideKeyBoard(v);
            }
        });
        town_house.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _houseType = "Town House";
                hideKeyBoard(v);
            }
        });
        house.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _houseType = "House";
                hideKeyBoard(v);
            }
        });

        // Bedroom number
        bed_zero.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _bedroom_number = "0";
                hideKeyBoard(v);
            }
        });
        bed_one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _bedroom_number = "1";
                hideKeyBoard(v);
            }
        });
        bed_two.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _bedroom_number = "2";
                hideKeyBoard(v);
            }
        });
        bed_three.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _bedroom_number = "3";
                hideKeyBoard(v);
            }
        });
        bed_four_plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _bedroom_number = "4+";
                hideKeyBoard(v);
            }
        });

        // Bathroom number
        bath_zero.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _bathroom_number = "0";
                hideKeyBoard(v);
            }
        });
        bath_one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _bathroom_number = "1";
                hideKeyBoard(v);
            }
        });
        bath_two.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _bathroom_number = "2";
                hideKeyBoard(v);
            }
        });
        bath_three.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _bathroom_number = "3";
                hideKeyBoard(v);
            }
        });
        bath_four_plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _bathroom_number = "4+";
                hideKeyBoard(v);
            }
        });

        // Lease length
        annual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _leaseLength = "Annually";
                hideKeyBoard(v);
            }
        });
        quarterly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _leaseLength = "Quarterly";
                hideKeyBoard(v);
            }
        });
        short_term.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _leaseLength = "Monthly";
                hideKeyBoard(v);
            }
        });

        // SUBMIT button !!!
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // validation
                if (validation())
                    return;

                _posterId = mAuth.getCurrentUser().getUid();

                // get edit text content
                _title = title.getText().toString();

                for (int i = 0; i < roomList.size(); i++)
                    roomList.get(i).setPrice(Integer.parseInt(((EditText) roomBoxList.get(i).findViewById(R.id.price)).getText().toString()));
                _location = street.getText().toString() + ", " + city.getText().toString();
                _startDate = start_date.getText().toString();
                _description = description.getText().toString();

                // Facilities check box
                _ac = isChecked(ac);
                _pet = isChecked(allow_pet);
                _parking = isChecked(parking);
                _tv = isChecked(tv);
                _videoGame = isChecked(video_game);
                _gym = isChecked(gym);
                _laundry = isChecked(laundry);
                _bus = isChecked(bus);

                uploadToFirebase(pictureList);
                finish();
            }
        });

        // toolbar setup
        /*toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);*/
        // back button setup
        ImageView backBtn = findViewById(R.id.back_button);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // TODO initialize (Maybe put it before the submit listener)
        // get input house post
        Bundle bundle = getIntent().getExtras();
        _postId = bundle.getString("postID");

        // Get house post from database
        mDatabase.child("House_post").child(_postId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // here
                    old_post = dataSnapshot.getValue(House.class);
                    initializeFields();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }// on create

    // helper for intiializing the fields
    private void initializeFields() {


        title.setText(old_post.getTitle());

        String[] street_and_city = old_post.getLocation().split(",");
        String old_street = "";
        String old_city = "";
        for (int i = 0; i < street_and_city.length; i++) {
            if (i < street_and_city.length - 1)
                old_street += street_and_city[i];
            else
                old_city += street_and_city[i];
        }
        street.setText(old_street);
        city.setText(old_city);

        SegmentedGroup houseType = findViewById(R.id.house_type);
        _houseType = old_post.getHouseType();
        switch (_houseType) {
            case "Apartment":
                houseType.check(R.id.apartment);
                break;
            case "House":
                houseType.check(R.id.house);
                break;
            case "Town House":
                houseType.check(R.id.town_house);
                break;
        }

        SegmentedGroup bedNum = findViewById(R.id.bedroom);
        _bedroom_number = old_post.getNumBedroom();
        switch (_bedroom_number) {
            case "0":
                bedNum.check(R.id.bed_zero);
                break;
            case "1":
                bedNum.check(R.id.bed_one);
                break;
            case "2":
                bedNum.check(R.id.bed_two);
                break;
            case "3":
                bedNum.check(R.id.bed_three);
                break;
            case "4+":
                bedNum.check(R.id.bed_four_plus);
                break;
        }

        SegmentedGroup bathNum = findViewById(R.id.bathroom);
        _bathroom_number = old_post.getNumBathroom();
        switch (_bathroom_number) {
            case "0":
                bathNum.check(R.id.bath_zero);
                break;
            case "1":
                bathNum.check(R.id.bath_one);
                break;
            case "2":
                bathNum.check(R.id.bath_two);
                break;
            case "3":
                bathNum.check(R.id.bath_three);
                break;
            case "4+":
                bathNum.check(R.id.bath_four_plus);
                break;
        }

        SegmentedGroup leaseLength = findViewById(R.id.lease_length);
        _leaseLength = old_post.getLeasingLength();
        switch (_leaseLength) {
            case "Annually":
                leaseLength.check(R.id.annual);
                break;
            case "Quarterly":
                leaseLength.check(R.id.quarterly);
                break;
            case "Monthly":
                leaseLength.check(R.id.short_term);
                break;
        }

        start_date.setText(old_post.getStartDate());

        setCheckedTextView_fromResult(ac, old_post.getAc());
        setCheckedTextView_fromResult(allow_pet, old_post.getPet());
        setCheckedTextView_fromResult(parking, old_post.getParking());
        setCheckedTextView_fromResult(tv, old_post.getTv());
        setCheckedTextView_fromResult(video_game, old_post.getParking());
        setCheckedTextView_fromResult(gym, old_post.getGym());
        setCheckedTextView_fromResult(laundry, old_post.getLaundry());
        setCheckedTextView_fromResult(bus, old_post.getBus());

        description.setText(old_post.getDescription());

        for (String pic : old_post.getPictures()) {
            addPhoto(Uri.parse(pic));
        }
        for (Room r : old_post.getRooms())
            addRoom(r);
    }

    // helper for date
    private void setDate(final Calendar calendar) {
        final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
        start_date.setText(dateFormat.format(calendar.getTime()));
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar cal = new GregorianCalendar(year, month, dayOfMonth);
        setDate(cal);
    }

    // helper validation
    private boolean validation() {
        if (!filledIn || start_date.getText().toString().length() == 0) {
            String INVALID_FORM_DUE_TO_FILLING = "Please fill in all the information!";
            Toast.makeText(EditPostActivity.this, INVALID_FORM_DUE_TO_FILLING, Toast.LENGTH_LONG).show();
            return true;
        }
        if (roomBoxList.isEmpty() || photoBoxList.isEmpty()) {
            String INVALID_FORM_DUE_TO_ADDING = "Please add at least one picture and one room information!";
            Toast.makeText(EditPostActivity.this, INVALID_FORM_DUE_TO_ADDING, Toast.LENGTH_LONG).show();
            return true;
        }
        String INVALID_FORM_DUE_TO_SELECTING = "Please select all the choices!";
        if (_houseType.length() == 0 || _bedroom_number.length() == 0
                || _bathroom_number.length() == 0 || _leaseLength.length() == 0) {
            Toast.makeText(EditPostActivity.this, INVALID_FORM_DUE_TO_SELECTING, Toast.LENGTH_LONG).show();
            return true;
        }
        for (Room r : roomList) {
            if (r.getRoomType().length() == 0) {
                Toast.makeText(EditPostActivity.this, INVALID_FORM_DUE_TO_SELECTING, Toast.LENGTH_LONG).show();
                return true;
            }
        }
        return false;
    }

    // add room
    private void addRoom(Room r) {
        final LinearLayout roomBox = (LinearLayout) View.inflate(this,
                R.layout.add_room, null);
        final Room room = new Room("", 0);
        roomBox.findViewById(R.id.deleteroom_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = roomBoxList.indexOf(roomBox);
                roomBoxList.remove(index);
                roomList.remove(index);
                roomContainer.removeView(roomBox);
                hideKeyBoard(v);
            }
        });

        // Room type
        roomBox.findViewById(R.id.master_bedroom).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: change variable
                room.setRoomType("Master Bedroom");
                hideKeyBoard(v);
            }
        });
        roomBox.findViewById(R.id.living_room).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                room.setRoomType("Living Room");
                hideKeyBoard(v);
            }
        });
        roomBox.findViewById(R.id.loft).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                room.setRoomType("Loft / Den");
                hideKeyBoard(v);
            }
        });

        SegmentedGroup roomType = roomBox.findViewById(R.id.room_type);
        room.setRoomType(r.getRoomType());
        switch (room.getRoomType()) {
            case "Master Bedroom":
                roomType.check(roomBox.findViewById(R.id.master_bedroom).getId());
                break;
            case "Living Room":
                roomType.check(roomBox.findViewById(R.id.living_room).getId());
                break;
            case "Loft / Den":
                roomType.check(roomBox.findViewById(R.id.loft).getId());
                break;
        }

        EditText price = roomBox.findViewById(R.id.price);
        if (r.getPrice() != -1)
            price.setText(String.valueOf(r.getPrice()));
        price.addTextChangedListener(tw);
        roomBoxList.add(roomBox);
        roomList.add(room);
        roomContainer.addView(roomBox);
    }

    // add photo
    private void addPhoto(Uri uri) {
        final LinearLayout photoBox = (LinearLayout) View.inflate(this,
                R.layout.add_photo, null);
        photoBox.findViewById(R.id.deletepic_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = photoBoxList.indexOf(photoBox);
                hideKeyBoard(v);
                if (pictureList.get(index).toString().contains("https")) {
                    FirebaseStorage.getInstance().getReferenceFromUrl(pictureList.get(index).toString()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // File deleted successfully
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Uh-oh, an error occurred!
                        }
                    });
                }
                photoBoxList.remove(index);
                pictureList.remove(index);
                photoGrid.removeView(photoBox);
            }
        });
        photoBoxList.add(photoBox);

        // Mandatory to refresh image from Uri.
        // upload to firebase storage
        Uri picture = uri;
        Glide.with(EditPostActivity.this)
                .load(picture)
                .apply(RequestOptions.centerCropTransform())
                .into((ImageView) photoBoxList.get(photoBoxList.size() - 1).findViewById(R.id.pic));
        // add photo box to UI
        photoGrid.addView(photoBox);
        pictureList.add(picture);
    }

    //PickImage Plug-in
    //choose picture from camera or gallery
    @Override
    public void onPickResult(PickResult r) {
        if (r.getError() == null) {
            // create photo box
            addPhoto(r.getUri());
        } else {
            Toast.makeText(this, r.getError().getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // hide kb
    private void hideKeyBoard(View view) {
        View vv = findViewById(android.R.id.content);
        if (vv != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // write to firebase
    private void writeNewPost(House house) {
        post.setValue(house);
    }

    private void uploadToFirebase(ArrayList<Uri> uriList) {
        post = mDatabase.child("House_post").child(_postId);
        ArrayList<String> old_uri = new ArrayList<>();
        int startIndex = -1;
        for (int i = 0; i < uriList.size(); i++) {
            if (uriList.get(i).toString().contains("https"))
                old_uri.add(uriList.get(i).toString());
            else {
                startIndex = i;
                break;
            }
        }
        writeNewPost(new House(_posterId, _houseType, _title, _location,
                _description, _startDate, _leaseLength,
                old_uri, roomList, _tv, _ac, _bus,
                _parking, _videoGame, _gym, _laundry, _pet, _bedroom_number, _bathroom_number));

        final StorageReference baseref = mStorageRef.child("House_Images").child(_postId);
        StorageReference image_ref;
        final ArrayList<String> pictureStringList = new ArrayList<>(0);
        int i = 0;
        for (Uri uri : uriList) {
            final int finalI = i;
            i++;
            if (startIndex >= i)
                continue;
            image_ref = baseref.child(UUID.randomUUID().toString());
            final StorageReference finalImage_ref = image_ref;

            try {
                image_ref.putFile(uri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                // Get a URL to the uploaded content
                                finalImage_ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        post.child("pictures").child(String.valueOf(finalI)).setValue(uri.toString());
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {// Handle any errors
                                    }
                                });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {// Handle unsuccessful uploads
                                // ...
                            }
                        });
            } catch (Exception e) {
                Log.d("InitiateProfile", e.getMessage());
            }
        }
    }

    public static class DatePickerFragment extends DialogFragment {
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            return new DatePickerDialog(getActivity(),
                    (DatePickerDialog.OnDateSetListener) getActivity(), year, month, day);
        }

    }

    class MyTextWatcher implements TextWatcher {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (title.getText().toString().length() > 0 &&
                    street.getText().toString().length() > 0 &&
                    city.getText().toString().length() > 0 &&
                    description.getText().toString().length() > 0) {
                for (LinearLayout layout : roomBoxList)
                    if (((EditText) layout.findViewById(R.id.price)).getText().toString().length() <= 0) {
                        filledIn = false;
                        return;
                    }
                filledIn = true;
            } else {
                filledIn = false;
            }
        }
    }
}
