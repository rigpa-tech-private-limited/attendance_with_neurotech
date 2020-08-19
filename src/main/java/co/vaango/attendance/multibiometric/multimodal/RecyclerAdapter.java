package co.vaango.attendance.multibiometric.multimodal;

import co.vaango.attendance.R;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {
    private ArrayList<Visit> arrayList = new ArrayList<Visit>();

    public  RecyclerAdapter(ArrayList<Visit> arrayList){
    this.arrayList = arrayList;

    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.visit_item_view,viewGroup,false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder viewHolder, int i) {
        Log.d("facecheck","getName = "+arrayList.get(i).getName());
        viewHolder.Name.setText(arrayList.get(i).getName());
        int sync_status = arrayList.get(i).getSync_status();
        if(sync_status == DbVisit.SYNC_STATUS_OK){
            viewHolder.SyncStatus.setImageResource(R.drawable.ok);
        } else {
            viewHolder.SyncStatus.setImageResource(R.drawable.sync);
        }
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        ImageView SyncStatus;
        TextView Name;

        public MyViewHolder(View itemView){
            super(itemView);
            SyncStatus = (ImageView) itemView.findViewById(R.id.imgSync);
            Name = (TextView) itemView.findViewById(R.id.txtName);
        }
    }
}
