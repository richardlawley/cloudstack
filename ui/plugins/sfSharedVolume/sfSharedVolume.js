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
          size: { label: 'Size' },
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
          delete: {
            label: "Delete Shared Volume",
            messages: {
              confirm: function() { return 'Are you sure you want to delete this shared volume?' },
              notification: function() { return 'Deleted shared volume' }
            },
            action: function(args) {
              var instance = args.context.sfSharedVolumes[0];
            }
          }
        }
      },
      actions: {
        add: {
          label: 'Add Shared Volume',
          preFilter: function(args) {
            return true;
          },
          messages: {
            notification: function(args) {
              return 'Add Shared Volume';
            }
          }
        }
      }
    });
  };
}(cloudStack));