(function (cloudStack) {
  cloudStack.plugins.sfSharedVolume = function(plugin) {
    plugin.ui.addSection({
      id: 'sfSharedVolume',
      title: 'Shared Volume',
      preFilter: function(args) {
        return true;
      },
      listView: {
        id: 'sfClusters',
        fields: {
          name: { label: 'label.name' },
          mvip: { label: 'MVIP' },
          username: { label: 'label.username' },
          zonename: { label: 'label.zone.name' }
        },
        dataProvider: function(args) {
          plugin.ui.apiCall('listSolidFireClusters', {
            success: function(json) {
              var sfclusters = json.listsolidfireclustersresponse.sfcluster;

              args.response.success({ data: sfclusters });
            },
            error: function(errorMessage) {
              args.response.error(errorMessage)
            }
          });
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