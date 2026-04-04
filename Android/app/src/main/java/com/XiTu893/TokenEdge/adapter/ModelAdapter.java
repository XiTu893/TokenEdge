package com.XiTu893.TokenEdge.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.XiTu893.TokenEdge.R;
import com.XiTu893.TokenEdge.model.ModelConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModelAdapter extends RecyclerView.Adapter<ModelAdapter.ViewHolder> {

    private List<ModelConfig> models;
    private List<ModelConfig> originalModels;
    private int totalRAMGB = 0;
    private OnModelActionListener listener;
    private Map<String, ModelConfig.ModelStatus> modelStatusMap;
    private Map<String, Integer> selectedContextMap;
    private Map<String, String> modelPathMap;
    private String runningModelId = null;

    public interface OnModelActionListener {
        void onDownload(ModelConfig model);
        void onStart(ModelConfig model, int contextSize);
        void onStop(ModelConfig model);
    }

    public ModelAdapter(List<ModelConfig> models, int totalRAMGB, OnModelActionListener listener) {
        this.originalModels = models;
        this.models = new ArrayList<>(models);
        this.totalRAMGB = totalRAMGB;
        this.listener = listener;
        this.modelStatusMap = new HashMap<>();
        this.selectedContextMap = new HashMap<>();
        this.modelPathMap = new HashMap<>();
        
        for (ModelConfig model : models) {
            modelStatusMap.put(model.id, ModelConfig.ModelStatus.NOT_DOWNLOADED);
            selectedContextMap.put(model.id, model.defaultContextIndex);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_model, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ModelConfig model = models.get(position);
        bindModel(holder, model);
    }

    private void bindModel(ViewHolder holder, ModelConfig model) {
        holder.tvName.setText(model.displayName);
        holder.tvDesc.setText(model.description);
        holder.tvSize.setText("大小: " + model.sizeGB + "GB");
        holder.tvRAM.setText("推荐: " + model.recommendedRAMGB + "GB+ RAM");

        updateStatusBadge(holder, model);
        updateRAMRequirementColor(holder, model);
        updateActionButtons(holder, model);
        setupContextRadioGroup(holder, model);
    }

    private void updateStatusBadge(ViewHolder holder, ModelConfig model) {
        ModelConfig.ModelStatus status = modelStatusMap.getOrDefault(model.id, ModelConfig.ModelStatus.NOT_DOWNLOADED);
        
        switch (status) {
            case NOT_DOWNLOADED:
                holder.tvStatusBadge.setText("未下载");
                holder.tvStatusBadge.setBackgroundColor(0xFF666666);
                break;
            case DOWNLOADING:
                holder.tvStatusBadge.setText("下载中");
                holder.tvStatusBadge.setBackgroundColor(0xFFFF9800);
                break;
            case DOWNLOADED:
                holder.tvStatusBadge.setText("已下载");
                holder.tvStatusBadge.setBackgroundColor(0xFF4CAF50);
                break;
            case STARTING:
                holder.tvStatusBadge.setText("启动中");
                holder.tvStatusBadge.setBackgroundColor(0xFF2196F3);
                break;
            case RUNNING:
                holder.tvStatusBadge.setText("运行中");
                holder.tvStatusBadge.setBackgroundColor(0xFF4CAF50);
                break;
        }
    }

    private void updateRAMRequirementColor(ViewHolder holder, ModelConfig model) {
        if (totalRAMGB >= model.recommendedRAMGB) {
            holder.tvRAM.setBackgroundColor(0xFFC8E6C9);
            holder.tvRAM.setTextColor(0xFF2E7D32);
        } else if (totalRAMGB >= model.minRAMGB) {
            holder.tvRAM.setBackgroundColor(0xFFFFF3E0);
            holder.tvRAM.setTextColor(0xFFE65100);
        } else {
            holder.tvRAM.setBackgroundColor(0xFFFFEBEE);
            holder.tvRAM.setTextColor(0xFFB71C1C);
        }
    }

    private void updateActionButtons(ViewHolder holder, ModelConfig model) {
        ModelConfig.ModelStatus status = modelStatusMap.getOrDefault(model.id, ModelConfig.ModelStatus.NOT_DOWNLOADED);
        
        holder.downloadLayout.setVisibility(View.GONE);
        holder.contextLayout.setVisibility(View.GONE);
        holder.actionLayout.setVisibility(View.GONE);
        holder.btnStart.setVisibility(View.GONE);
        holder.btnStop.setVisibility(View.GONE);

        switch (status) {
            case NOT_DOWNLOADED:
                holder.downloadLayout.setVisibility(View.VISIBLE);
                break;
            case DOWNLOADING:
                break;
            case DOWNLOADED:
                if (!isAnotherModelRunning(model.id)) {
                    holder.contextLayout.setVisibility(View.VISIBLE);
                    holder.actionLayout.setVisibility(View.VISIBLE);
                    holder.btnStart.setVisibility(View.VISIBLE);
                }
                break;
            case STARTING:
                break;
            case RUNNING:
                if (runningModelId != null && runningModelId.equals(model.id)) {
                    holder.contextLayout.setVisibility(View.VISIBLE);
                    holder.actionLayout.setVisibility(View.VISIBLE);
                    holder.btnStop.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    private void setupContextRadioGroup(ViewHolder holder, ModelConfig model) {
        holder.contextRadioGroup.removeAllViews();
        
        int selectedIndex = selectedContextMap.getOrDefault(model.id, model.defaultContextIndex);
        
        for (int i = 0; i < model.contextOptions.length; i++) {
            RadioButton radioButton = new RadioButton(holder.itemView.getContext());
            radioButton.setText(formatContextSize(model.contextOptions[i]));
            radioButton.setId(i);
            
            RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(
                RadioGroup.LayoutParams.WRAP_CONTENT,
                RadioGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 16, 0);
            radioButton.setLayoutParams(params);
            
            if (i == selectedIndex) {
                radioButton.setChecked(true);
            }
            
            final int index = i;
            radioButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedContextMap.put(model.id, index);
                }
            });
            
            holder.contextRadioGroup.addView(radioButton);
        }
    }

    private String formatContextSize(int size) {
        if (size >= 1024) {
            return (size / 1024) + "K";
        }
        return size + "";
    }

    private boolean isAnotherModelRunning(String modelId) {
        return runningModelId != null && !runningModelId.equals(modelId);
    }

    public void setModelStatus(String modelId, ModelConfig.ModelStatus status) {
        modelStatusMap.put(modelId, status);
        if (status == ModelConfig.ModelStatus.RUNNING) {
            runningModelId = modelId;
        } else if (status == ModelConfig.ModelStatus.DOWNLOADED && runningModelId != null && runningModelId.equals(modelId)) {
            runningModelId = null;
        }
        notifyDataSetChanged();
    }

    public void setModelDownloaded(String modelId, String modelPath) {
        modelStatusMap.put(modelId, ModelConfig.ModelStatus.DOWNLOADED);
        modelPathMap.put(modelId, modelPath);
        notifyDataSetChanged();
    }

    public void setTotalRAM(int ramGB) {
        this.totalRAMGB = ramGB;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvStatusBadge;
        TextView tvDesc;
        TextView tvSize;
        TextView tvRAM;
        TextView btnDownload;
        TextView btnStart;
        TextView btnStop;
        View downloadLayout;
        View contextLayout;
        View actionLayout;
        RadioGroup contextRadioGroup;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvModelName);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            tvDesc = itemView.findViewById(R.id.tvModelDesc);
            tvSize = itemView.findViewById(R.id.tvModelSize);
            tvRAM = itemView.findViewById(R.id.tvModelRAM);
            btnDownload = itemView.findViewById(R.id.btnDownload);
            btnStart = itemView.findViewById(R.id.btnStart);
            btnStop = itemView.findViewById(R.id.btnStop);
            downloadLayout = itemView.findViewById(R.id.downloadLayout);
            contextLayout = itemView.findViewById(R.id.contextLayout);
            actionLayout = itemView.findViewById(R.id.actionLayout);
            contextRadioGroup = itemView.findViewById(R.id.contextRadioGroup);

            btnDownload.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    ModelConfig model = models.get(position);
                    listener.onDownload(model);
                }
            });

            btnStart.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    ModelConfig model = models.get(position);
                    int contextIndex = selectedContextMap.getOrDefault(model.id, model.defaultContextIndex);
                    listener.onStart(model, model.contextOptions[contextIndex]);
                }
            });

            btnStop.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    ModelConfig model = models.get(position);
                    listener.onStop(model);
                }
            });
        }
    }
}
