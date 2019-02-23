package com.imastudio.mapandroid;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PlacePickerActivity extends AppCompatActivity {

    @BindView(R.id.btnplace)
    Button btnplace;
    @BindView(R.id.txtdetailalamat)
    TextView txtdetailalamat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_picker);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btnplace)
    public void onViewClicked() {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try{
            startActivityForResult(builder.build(PlacePickerActivity.this),1);
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
            if (requestCode==1&&resultCode==RESULT_OK){
                Place p = PlacePicker.getPlace(PlacePickerActivity.this,data);
                String InformasiDetail =String.format("Place : %s \n" +
                        "alamat : %s \n latlong : %s"+p.getName(),p.getAddress(),
                        p.getLatLng().latitude+","+p.getLatLng().longitude);
                txtdetailalamat.setText(InformasiDetail);
            }
    }
}
