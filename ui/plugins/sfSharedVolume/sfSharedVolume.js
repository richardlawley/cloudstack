(function (cloudStack) {
  cloudStack.plugins.sfSharedVolume = function(plugin) {
    plugin.ui.addSection({
      id: 'sfSharedVolume',
      title: 'Shared Volume',
      preFilter: function(args) {
        return true;
      },
      listView: {
        id: 'sfSharedVolumes',
        fields: {
          name: { label: 'label.name' },
          iqn: { label: 'IQN' },
          size: { label: 'Size (GB)' },
          miniops: { label: 'Min IOPS' },
          maxiops: { label: 'Max IOPS' },
          burstiops: { label: 'Burst IOPS' }
        },
        dataProvider: function(args) {
          plugin.ui.apiCall('listSolidFireVolumes', {
            success: function(json) {
              var sfvolumes = json.listsolidfirevolumesresponse.sfvolume;

              args.response.success({ data: sfvolumes });
            },
            error: function(errorMessage) {
              args.response.error(errorMessage);
            }
          });
        },
        actions: {
          add: {
            label: 'Add Shared Volume',
            preFilter: function(args) {
              return true;
            },
            messages: {
              confirm: function(args) {
                return 'Please fill in the following data to add a new shared volume.';
              },
              notification: function(args) {
                return 'Add Shared Volume';
              }
            },
            createForm: {
              title: 'Add Shared Volume',
              desc: 'Please fill in the following data to add a new shared volume.',
              fields: {
                name: {
                  docID: 'helpVolumeName',
                  label: 'label.name',
                  validation: {
                    required: true
                  }
                },
                diskSize: {
                  label: 'label.disk.size.gb',
                  validation: {
                    required: true,
                    number: true
                  }
                },
                minIops: {
                  label: 'label.disk.iops.min',
                  validation: {
                    required: false,
                    number: true
                  }
                },
                maxIops: {
                  label: 'label.disk.iops.max',
                  validation: {
                    required: false,
                    number: true
                  }
                },
                burstIops: {
                  label: 'Burst IOPS',
                  validation: {
                    required: false,
                    number: true
                  }
                }
              }
            }
          }
        },
        detailView: {
          name: 'Shared volume details',
          isMaximized: true,
          actions: {
            edit: {
              label: 'Edit shared volume',
              compactLabel: 'label.edit',
              action: function(args) {
                var sharedVolumeObj = args.context;
              }
            }
          }
        }
      }
    });
  };
}(cloudStack));