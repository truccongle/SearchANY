package com.example.truccongle.searchanything.Activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.truccongle.searchanything.Adapter.KhuVuiChoiAdapter;
import com.example.truccongle.searchanything.Model.DiaDiem;
import com.example.truccongle.searchanything.R;
import com.example.truccongle.searchanything.Util.CheckConnection;
import com.example.truccongle.searchanything.Util.Server;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class KhuVuiChoiActivity extends AppCompatActivity {
    Toolbar toolbarKhuVuiChoi;
    ListView listViewKhuVuiChoi;
    KhuVuiChoiAdapter khuVuiChoiAdapter;
    ArrayList<DiaDiem> mangKhuVuiChoi;
    int idKhuVuiChoi =0;
    int page=1;
    View footterview;
    boolean isLoading= false;
    mHandler mHandler;
    boolean limitData=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_khu_vui_choi);
        AnhXa();
        if (CheckConnection.haveNetworkConnection(getApplicationContext())) {
            GetIdQuanNuoc();
            ActionToolbar();
            GetData(page);
            LoadMoreData();

        } else {
            CheckConnection.ShowToast_short(getApplicationContext(),"Hãy kiểm tra Internet");
            finish();

        }
    }

    private void LoadMoreData() {
        listViewKhuVuiChoi.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent= new Intent(getApplicationContext(),DiaDiemChiTietActivity.class);
                intent.putExtra("thongtindiadiem",mangKhuVuiChoi.get(position));
                startActivity(intent);
            }
        });
        listViewKhuVuiChoi.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem+visibleItemCount==totalItemCount&& totalItemCount!=0&&isLoading==false&&limitData==false){
                    isLoading=true;
                    ThreadData  threadData= new ThreadData();
                    threadData.start();
                }
            }
        });
    }

    private void GetData(int Page) {
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        String URL= Server.URLDiaDiem+String.valueOf(Page);
        StringRequest stringRequest= new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                int Ma=0;
                String Ten="";
                String HinhAnh="";
                String DiaChi="";
                String ThoiGian="";
                String SDT="";
                String DanhGia="";
                String LatLng="";
                int IdLoaiDiaDiem=0;
                if (response!=null&&response.length()!=2){
                    listViewKhuVuiChoi.removeFooterView(footterview);
                    try {
                        JSONArray jsonArray= new JSONArray(response);
                        for (int i=0;i<jsonArray.length();i++){
                            JSONObject jsonObject=jsonArray.getJSONObject(i);
                            Ma=jsonObject.getInt("ma");
                            Ten=jsonObject.getString("ten");
                            HinhAnh=jsonObject.getString("hinhanh");
                            DiaChi=jsonObject.getString("diachi");
                            ThoiGian=jsonObject.getString("thoigian");
                            SDT=jsonObject.getString("sdt");
                            DanhGia=jsonObject.getString("danhgia");
                            LatLng=jsonObject.getString("latlng");
                            IdLoaiDiaDiem=jsonObject.getInt("idloaidiadiem");
                            mangKhuVuiChoi.add( new DiaDiem(Ma,Ten,HinhAnh,DiaChi,ThoiGian,SDT,DanhGia,LatLng,IdLoaiDiaDiem));
                            khuVuiChoiAdapter.notifyDataSetChanged();

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                else {
                    limitData= true;
                    listViewKhuVuiChoi.removeFooterView(footterview);
                    CheckConnection.ShowToast_short(getApplicationContext(),"Đã tới trang cuối");

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                CheckConnection.ShowToast_short(getApplicationContext(),"Loi roi........-=");

            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> param=new HashMap<String, String>();
                param.put("idloaidiadiem",String.valueOf(idKhuVuiChoi));
                return param;
            }
        };
        requestQueue.add(stringRequest);
    }

    private void ActionToolbar() {
        setSupportActionBar(toolbarKhuVuiChoi);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbarKhuVuiChoi.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void GetIdQuanNuoc() {
        idKhuVuiChoi=getIntent().getIntExtra("idloaidiadiem",-1);
        Log.d("Gia tri loai dia diem",idKhuVuiChoi+"");
    }

    private void AnhXa() {
        toolbarKhuVuiChoi=(Toolbar) findViewById(R.id.toolbarKhuVuiChoi);
        listViewKhuVuiChoi=(ListView) findViewById(R.id.lvKhuVuiChoi);
        mangKhuVuiChoi= new ArrayList<>();
        khuVuiChoiAdapter=new KhuVuiChoiAdapter(getApplicationContext(),mangKhuVuiChoi);
        listViewKhuVuiChoi.setAdapter(khuVuiChoiAdapter);
        LayoutInflater inflater=(LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        footterview= inflater.inflate(R.layout.progressbar,null);
        mHandler=new mHandler();
    }
    public  class  mHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    listViewKhuVuiChoi.addFooterView(footterview);
                    break;
                case 1:

                    GetData(++page);
                    isLoading=false;
                    break;
            }
            super.handleMessage(msg);
        }
    }
    public  class  ThreadData extends  Thread{
        @Override
        public void run() {
            mHandler.sendEmptyMessage(0);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Message message= mHandler.obtainMessage(1);
            mHandler.sendMessage(message);
            super.run();
        }
    }
}
