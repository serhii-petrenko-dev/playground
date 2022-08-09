package io.xps.playground.ui.feature.overlay;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import io.xps.playground.databinding.ItemNotificationBinding;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> {

    private final Long messageDisplayTime = 3000L;
    private boolean handlerBusy = false;

    private final Handler handler;
    private ArrayList<String> notificationsList;

    private RecyclerView recycler;

    NotificationsAdapter() {
        handler = new Handler();
        notificationsList = new ArrayList<>();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        recycler = recyclerView;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        ItemNotificationBinding binding = ItemNotificationBinding.inflate(LayoutInflater.from(context));
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.binding.text.setText(notificationsList.get(position));
    }

    @Override
    public int getItemCount() {
        return notificationsList.size();
    }

    public void displayBubble(String message){
        notificationsList.add(message);
        notifyItemInserted(notificationsList.size());

        Log.d("moveOverlay", "displayBubble");

        if(!handlerBusy) {
            handlerBusy = true;
            recycler.setVisibility(View.VISIBLE);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.d("moveOverlay", "displayBubble run " + notificationsList.size() );
                    if (!notificationsList.isEmpty()) {
                        Log.d("moveOverlay", "displayBubble hideTopBubble");
                        hideTopBubble();
                        handler.postDelayed(this, messageDisplayTime);
                    } else {
                        Log.d("moveOverlay", "displayBubble removeCallbacks");
                        recycler.setVisibility(View.GONE);
                        handlerBusy = false;
                        handler.removeCallbacks(this);
                    }
                }
            }, messageDisplayTime);
        }
    }

    private void hideTopBubble() {
        notificationsList.remove(0);
        notifyItemRemoved(0);
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {

        ItemNotificationBinding binding;

        ViewHolder(ItemNotificationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
