#!/bin/bash

gcloud compute instances create llm-server \
    --project=your-gcp-project-id \
    --zone=europe-west6-b \
    --machine-type=g2-standard-4 \
    --network-interface=network-tier=PREMIUM,stack-type=IPV4_ONLY,subnet=default \
    --metadata=enable-osconfig=TRUE \
    --maintenance-policy=TERMINATE \
    --provisioning-model=STANDARD \
    --service-account=your-service-account@developer.gserviceaccount.com \
    --scopes=https://www.googleapis.com/auth/devstorage.read_write,... \
    --accelerator=count=1,type=nvidia-l4 \
    --tags=http-server,https-server \
    --create-disk=auto-delete=yes,boot=yes,device-name=llm-server,image=projects/your-project/global/images/your-custom-image,mode=rw,size=100,type=pd-ssd \
    --no-shielded-secure-boot \
    --shielded-vtpm \
    --shielded-integrity-monitoring \
    --labels=your-label-key=your-label-value \
    --reservation-affinity=any \
&& \
printf 'agentsRule:\n  packageState: installed\n  version: latest\ninstanceFilter:\n  inclusionLabels:\n  - labels:\n      your-label-key: your-label-value\n' > config.yaml \
&& \
gcloud compute instances ops-agents policies create your-policy-name \
    --project=your-gcp-project-id \
    --zone=europe-west6-b \
    --file=config.yaml
