import torch
print(f"{torch.cuda.is_available() = }")
print(f"{torch.cuda.current_device() = }")
print(f"{torch.cuda.get_device_name(0) = }")
print(f"{torch.version.cuda = }")