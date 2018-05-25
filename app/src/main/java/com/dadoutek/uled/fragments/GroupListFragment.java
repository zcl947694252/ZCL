package com.dadoutek.uled.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.dadoutek.uled.model.DbModel.DBUtils;
import com.dadoutek.uled.model.DbModel.DbGroup;
import com.dadoutek.uled.R;
import com.dadoutek.uled.TelinkLightApplication;
import com.dadoutek.uled.TelinkLightService;
import com.dadoutek.uled.activity.AddMeshActivity;
import com.dadoutek.uled.activity.GroupSettingActivity;
import com.dadoutek.uled.activity.LightsOfGroupActivity;
import com.dadoutek.uled.activity.SelectDeviceTypeActivity;
import com.dadoutek.uled.model.Constant;
import com.dadoutek.uled.util.DataManager;

import java.util.ArrayList;
import java.util.List;

public final class GroupListFragment extends Fragment {

    private LayoutInflater inflater;
    private GroupListAdapter adapter;

    private Activity mContext;
    private TelinkLightApplication mApplication;
    private DataManager dataManager;
    private List<DbGroup> gpList;
    private TelinkLightApplication application;

    private OnItemLongClickListener itemLongClickListener = new OnItemLongClickListener() {

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view,
                                       int position, long id) {

//            Group men_group = adapter.getItem(position);
//
//            Intent intent = new Intent(mContext, GroupSettingActivity.class);
//            intent.putExtra("groupAddress", men_group.meshAddress);
//
//            startActivityForResult(intent, 0);

            return true;
        }
    };
    private GridView gridView;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == Constant.RESULT_OK) {
            this.initData();
            this.notifyDataSetChanged();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.mContext = this.getActivity();
        setHasOptionsMenu(true);

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("dadougg", "onResume: ");
        this.initData();
        this.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        this.inflater = inflater;

        View view = inflater.inflate(R.layout.fragment_group_list, null);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.group_list_header);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        gridView = (GridView) view.findViewById(R.id.list_groups);
        gridView.setOnItemLongClickListener(this.itemLongClickListener);

        this.initData();
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        getActivity().getMenuInflater().inflate(R.menu.men_group, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_setting:
                Intent intent = new Intent(mContext, AddMeshActivity.class);
                startActivity(intent);
                break;

            case R.id.menu_add:
                TelinkLightService.Instance().idleMode(true);
                startActivity(new Intent(mContext, SelectDeviceTypeActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if (!hidden) {
            this.initData();
        }
    }

    private void initData() {
        this.mApplication = (TelinkLightApplication) getActivity().getApplication();
        gpList= DBUtils.getGroupList();
        this.adapter = new GroupListAdapter(gpList);
        gridView.setAdapter(this.adapter);
        application= (TelinkLightApplication) getActivity().getApplication();
        dataManager=new DataManager(TelinkLightApplication.getInstance(),
                application.getMesh().name,application.getMesh().password);
    }

    public void notifyDataSetChanged() {
        this.adapter.notifyDataSetChanged();
    }

    private static class GroupItemHolder {
        public TextView txtName;
        public TextView btnOn;
        public TextView btnOff;
        public TextView btnSet;
    }

    final class GroupListAdapter extends BaseAdapter implements
            OnClickListener, OnLongClickListener {
        ArrayList<DbGroup> groupArrayListNew = new ArrayList<>();

        public GroupListAdapter(List<DbGroup> groups) {
            if(groupArrayListNew.size()>0){
                groupArrayListNew.clear();
            }
            List<DbGroup> groupList = groups;
            for (DbGroup group : groupList) {
                groupArrayListNew.add(group);
            }
        }

        @Override
        public boolean isEnabled(int position) {
            return false;
        }

        @Override
        public int getCount() {
            return groupArrayListNew.size();
        }

        @Override
        public DbGroup getItem(int position) {
            return groupArrayListNew.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            GroupItemHolder holder;

            if (convertView == null) {

                convertView = inflater.inflate(R.layout.group_item, null);

                TextView txtName = (TextView) convertView
                        .findViewById(R.id.txt_name);
                txtName.setOnClickListener(this);

                TextView btnOn = (TextView) convertView.findViewById(R.id.btn_on);
                btnOn.setOnClickListener(this);

                TextView btnOff = (TextView) convertView.findViewById(R.id.btn_off);
                btnOff.setOnClickListener(this);

                TextView btnSet = (TextView) convertView.findViewById(R.id.btn_set);
                btnSet.setOnClickListener(this);

                holder = new GroupItemHolder();

                holder.txtName = txtName;
                holder.btnOn = btnOn;
                holder.btnOff = btnOff;
                holder.btnSet = btnSet;

                convertView.setTag(holder);

            } else {
                holder = (GroupItemHolder) convertView.getTag();
            }

            DbGroup group = this.getItem(position);

            if (group != null) {
                if (group.textColor == 0)
                    group.textColor = mContext.getResources()
                            .getColor(R.color.black);

                holder.txtName.setText(group.getName());
                holder.txtName.setTextColor(group.textColor);
                holder.txtName.setTag(position);
                holder.btnOn.setTag(position);
                holder.btnOff.setTag(position);
                holder.btnSet.setTag(position);
            }

            return convertView;
        }

        @Override
        public void onClick(View view) {

            int clickId = view.getId();
            int position = (int) view.getTag();

            DbGroup group=groupArrayListNew.get(position);

            byte opcode = (byte) 0xD0;
            int dstAddr = group.getMeshAddr();
            Intent intent;

            if (!dataManager.getConnectState(getActivity())) {
                return;
            }

            switch (clickId) {
                case R.id.btn_on:
                    TelinkLightService.Instance().sendCommandNoResponse(opcode, dstAddr,
                            new byte[]{0x01, 0x00, 0x00});
                    break;
                case R.id.btn_off:
                    TelinkLightService.Instance().sendCommandNoResponse(opcode, dstAddr,
                            new byte[]{0x00, 0x00, 0x00});
                    break;
                case R.id.btn_set:
                    intent = new Intent(mContext, GroupSettingActivity.class);
                    intent.putExtra("group", group);
                    startActivityForResult(intent, 0);
                    break;
                case R.id.txt_name:
                    intent = new Intent(mContext, LightsOfGroupActivity.class);
                    intent.putExtra("group", group);
                    startActivity(intent);
                    break;
            }
        }

        @Override
        public boolean onLongClick(View v) {

            return false;
        }
    }
}
