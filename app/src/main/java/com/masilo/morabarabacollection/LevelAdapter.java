package com.masilo.morabarabacollection;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class LevelAdapter extends RecyclerView.Adapter<LevelAdapter.LevelViewHolder> {

    private final List<Level> levels;
    private final OnLevelClickListener listener;

    public interface OnLevelClickListener {
        void onLevelClick(Level level);
    }

    public LevelAdapter(List<Level> levels, OnLevelClickListener listener) {
        this.levels = levels;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LevelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        return new LevelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LevelViewHolder holder, int position) {
        Level level = levels.get(position);
        holder.bind(level, listener);
    }

    @Override
    public int getItemViewType(int position) {
        Level level = levels.get(position);
        if (level.isCurrent()) {
            return R.layout.level_item_current;
        }
        return level.isUnlocked() ? R.layout.level_item_unlocked : R.layout.level_item_locked;
    }

    @Override
    public int getItemCount() {
        return levels.size();
    }

    public static class LevelViewHolder extends RecyclerView.ViewHolder {
        private final TextView levelName;
        private final TextView levelStatus;
        private final TextView levelPoints;
        private final ProgressBar levelProgress;
        private final Button playButton;

        public LevelViewHolder(@NonNull View itemView) {
            super(itemView);
            levelName = itemView.findViewById(R.id.level_name);
            levelStatus = itemView.findViewById(R.id.level_status);
            levelPoints = itemView.findViewById(R.id.level_points);
            levelProgress = itemView.findViewById(R.id.level_progress);
            playButton = itemView.findViewById(R.id.play_button);
        }

        public void bind(final Level level, final OnLevelClickListener listener) {
            levelName.setText(itemView.getContext().getString(R.string.level_format, level.getLevelNumber()));
            
            if (level.isCurrent()) {
                levelPoints.setText(itemView.getContext().getString(R.string.target_xp_format, level.getPoints()));
                levelStatus.setText(R.string.current_caps);
                playButton.setText(R.string.continue_text);
                playButton.setEnabled(true);
            } else {
                levelPoints.setText(itemView.getContext().getString(R.string.points_format, level.getPoints()));
                if (level.isUnlocked()) {
                    levelStatus.setText(R.string.unlock);
                    playButton.setText(R.string.play);
                    playButton.setEnabled(true);
                } else {
                    levelStatus.setText(R.string.locked);
                    playButton.setText(R.string.unlock);
                    playButton.setEnabled(false);
                }
            }
            
            levelProgress.setProgress(level.getProgress());
            playButton.setOnClickListener(v -> listener.onLevelClick(level));

            itemView.setOnClickListener(v -> Toast.makeText(itemView.getContext(), 
                    itemView.getContext().getString(R.string.level_format, level.getLevelNumber()), 
                    Toast.LENGTH_SHORT).show());
        }
    }
}
